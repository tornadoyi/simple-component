/**
 * 
 */
package com.simple.base.config;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.xml.Root;

import com.simple.base.logger.BaseLogger;
import com.simple.base.util.StringUtil;
import com.simple.base.xml.SimpleXmlParser;


/**
 * java SimpleXml的实现
 * @see <a href = "http://simple.sourceforge.net/download/stream/doc/examples/examples.php"> http://simple.sourceforge.net/download/stream/doc/examples/examples.php </a>
 * @author yongchao.zhao@happyelements.com
 * 2016年7月21日
 */
public class XmlConfig {
	public static final String XML_CONFIG_DIR_PATH_KAY = "XML_CONFIG_DIR_PATH";
	private static Map<Class<?>, Object> configCache = new ConcurrentHashMap<Class<?>, Object>();
    private static final String CLASS_FILE_NAME[] = {XmlConfig.class.getName().replaceAll("\\.", "/") + ".class"};

	private static final String[] SEARCH_CONFIG_DIR =
           {"WEB-INF/conf/", "www/WEB-INF/conf/","webroot/WEB-INF/conf/","webapp/WEB-INF/conf/",
            "../conf/"
           };
	
	
	private static File findFile(Class<?> clazz){
		Root root = clazz.getAnnotation(Root.class);
		
		String name = null;
		if(root != null) { 
			name = root.name();
		}
		if(StringUtil.isEmpty(name)) { 
			name = clazz.getSimpleName();
		}
		name = name + ".xml";
		BaseLogger.getLogger().debug("config file name is :" + name);
		
		String basePath  = System.getProperty(XML_CONFIG_DIR_PATH_KAY, "");
		String[] possiblePath = new String[SEARCH_CONFIG_DIR.length + 1];
        possiblePath[0] = basePath;
        System.arraycopy(SEARCH_CONFIG_DIR, 0, possiblePath, 1, SEARCH_CONFIG_DIR.length);
		
        File configFile = null;
        for (String classFileName : CLASS_FILE_NAME) {
            File startFile = getCurrentClassJarFile(classFileName);
            while (null != startFile) {
                for (String path : possiblePath) {
                	configFile = new File(startFile, path + File.separator + name);
                	BaseLogger.getLogger().debug("search path:" + configFile.getAbsolutePath());
                    if (configFile.exists()) break;
                }
    
                if (!configFile.exists()) {
                    startFile = startFile.getParentFile();
                } else {
                    break;
                }
            }
        }
		if(configFile == null){
			BaseLogger.getLogger().error("config file not found");
		}
		return configFile;
	}
	
	
    private static File getCurrentClassJarFile(String fileName) {
        ClassLoader classLoader = XmlConfig.class.getClassLoader();
        URL url = classLoader.getResource(fileName);

        String filePath = url.getFile();
        //如果是在一个jar文件中，则路径将以“！”作为jar包内外分隔
        String[] jarFilePath = filePath.split("!");
        File jarFile = new File(jarFilePath[0].replaceFirst("file:", ""));
        if (BaseLogger.isDebugEnabled()) {
        	BaseLogger.debug("Config file path is " + jarFile + ", file exists " + jarFile.exists());
        }
        return jarFile;
    }
	
	
	@SuppressWarnings("unchecked")
	public static <T> T getConfig(Class<T> clazz){
		
		if(configCache.containsKey(clazz)){
			T  t = (T)configCache.get(clazz);
			if(t != null){//由于有清空cache的操作，防止并发导致返回空值
				return t;
			}
		}
		File file = findFile(clazz);
		try {
			T t = SimpleXmlParser.resolveFile(file, clazz);
			configCache.put(clazz, t);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 清空配置文件缓存
	 */
	public static void flushConfigCache(){
		configCache.clear();
	}
	
	
	
	
	
	
	
	
}
