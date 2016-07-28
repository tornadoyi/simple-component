package com.simple.kv.reflect;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.simple.base.util.CollectionUtil;
import com.simple.base.util.StringUtil;


/**
 * JavaBean结构解析类
 * 
 * 
 */
public class BeanResolver {

	private final Class<?> beanClass;
	private final Map<String, PropertyInfo> propertyInfoMap;

	private BeanResolver(Class<?> beanClass, Map<String, PropertyInfo> propertyInfoMap) {
		this.beanClass = beanClass;
		this.propertyInfoMap = propertyInfoMap;
	}

	/**
	 * 获取bean中属性信息的集合
	 * 
	 * @return
	 */
	public Collection<PropertyInfo> getPropertyInfos() {
		return propertyInfoMap.values();
	}

	/**
	 * 获取bean中的某个属性信息
	 * 
	 * @param name
	 *            属性名
	 * @return
	 */
	public PropertyInfo getPropertyInfo(String name) {
		return propertyInfoMap.get(name);
	}

	/**
	 * 获取bean的属性名列表
	 * 
	 * @return 属性名列表
	 */
	public Set<String> getPropertyNameSet() {
		return propertyInfoMap.keySet();
	}

	/**
	 * 创建 一个新的bean对象
	 * 
	 * @return 一个新的bean对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T newInstance() {
		return (T)ReflectUtil.createInstance(beanClass);
	}

	private static final Map<Class<?>, BeanResolver> resolverCache = CollectionUtil.newCopyOnWriteHashMap();

	/**
	 * 获取Bean解析器
	 * 
	 * @param beanClass bean的Class
	 * @return 构造好的BeanResolver
	 */
	public static BeanResolver getInstance(Class<?> beanClass) {
		BeanResolver resolver = resolverCache.get(beanClass);
		if (resolver == null) {
			synchronized (beanClass) {
				resolver = resolverCache.get(beanClass);
				if (resolver == null) {
					resolver = resolveBean(beanClass);
					resolverCache.put(beanClass, resolver);
				}
			}
		}
		return resolver;
	}

	private static BeanResolver resolveBean(Class<?> beanClass) {
		PropertyDescriptor[] pds;
		try {
			pds = Introspector.getBeanInfo(beanClass).getPropertyDescriptors();
		} catch (IntrospectionException e) {
			pds = new PropertyDescriptor[0];
		}
		Map<String, PropertyInfo> pmap = CollectionUtil.newLinkedHashMap();
		for (PropertyDescriptor pd : pds) {
			String name = StringUtil.uncapitalize(pd.getName());
			if ("class".equals(name)) {
				continue;
			}
			PropertyInfo p = new PropertyInfo();
			p.setName(name);
			SpecialProperty sp = null;
			Type type = null;
			Method readMethod = pd.getReadMethod();
			if (readMethod != null && readMethod.getParameterTypes().length == 0) {
				p.setReadMethod(readMethod);
				if (sp == null) {
					sp = readMethod.getAnnotation(SpecialProperty.class);
				}
				if (type == null) {
					type = readMethod.getGenericReturnType();
				}
			}
			Method writeMethod = pd.getWriteMethod();
			if (writeMethod != null) {
				p.setWriteMethod(writeMethod);
				if (sp == null) {
					sp = writeMethod.getAnnotation(SpecialProperty.class);
				}
				Type[] types = writeMethod.getGenericParameterTypes();
				if (types != null && types.length > 0) {
					type = types[0];
				}
			}
			if(sp != null && sp.ignore()) { // 被忽略的属性
				continue;
			}
			p.setAnnoSpecialProperty(sp);
			if (type == null) {
				type = pd.getPropertyType();
				if(type == null) {
					continue;
				}
			}
			TypeInfo ti = TypeInfo.getInstance(type);
			p.setType(ti);
			pmap.put(name, p);
		}
		return new BeanResolver(beanClass, pmap);
	}

}
