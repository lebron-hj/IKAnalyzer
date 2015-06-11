/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 * 
 * 
 */
package org.wltea.analyzer.cfg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration 默认实现
 * 2012-5-8
 *
 */
public class DefaultConfig implements Configuration{
	
	/*
	 * 分词器默认字典路径 
	 */
	private static final Map<ResourceLoader,Configuration> coreConfigMap= new HashMap<ResourceLoader,Configuration>();
	
	/*
	 * 是否使用smart方式分词
	 */
	private boolean useSmart;
	private static Logger log = LoggerFactory.getLogger(DefaultConfig.class);
	private ResourceLoader resourceLoader;

	/*
	 * 初始化配置文件
	 */
	private DefaultConfig(ResourceLoader resourceLoader){	
		this.resourceLoader = resourceLoader;
	}

	
	/**
	 * 返回useSmart标志位
	 * useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * @return useSmart
	 */
	public boolean useSmart() {
		return useSmart;
	}

	/**
	 * 设置useSmart标志位
	 * useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * @param useSmart
	 */
	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}	
	
//	public static Configuration getInstance(ResourceLoader resourceLoader) {
//		if(coreConfigMap.containsKey(resourceLoader)){
//			return coreConfigMap.get(resourceLoader);
//		}
//		DefaultConfig cfg = new DefaultConfig(resourceLoader);
//		coreConfigMap.put(resourceLoader, cfg);
//		return cfg;
//	}
	
	public static Configuration getInstance(ResourceLoader resourceLoader) {
		if(coreConfigMap.containsKey(resourceLoader)){
			return coreConfigMap.get(resourceLoader);
		}
		DefaultConfig cfg = null;
		synchronized(coreConfigMap){
			if (!coreConfigMap.containsKey(resourceLoader)) {
				cfg = new DefaultConfig(resourceLoader);
				coreConfigMap.put(resourceLoader, cfg);
			}
		}
		return coreConfigMap.get(resourceLoader);
	}
			
	public ResourceLoader getResourceLoader(){
		return this.resourceLoader;
	}


	@Override
	public File getResourceFile(String resource) {
		if(this.getResourceLoader() instanceof SolrResourceLoader){
			SolrResourceLoader loader = (SolrResourceLoader) this.getResourceLoader();
			return new File((new StringBuilder()).append(loader.getConfigDir()).append(resource).toString());
		}
		return null;
	}

}
