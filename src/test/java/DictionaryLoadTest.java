import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.search.Query;
import org.apache.solr.core.SolrResourceLoader;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.query.IKQueryExpressionParser;


public class DictionaryLoadTest {

	public static void main(String[] args) {

		final int threadPoolCount = 1000;
		final int threadCount = threadPoolCount*threadPoolCount;
		String instanceDir1 = "solrCore1";
		String instanceDir2 = "solrCore2";
		final ResourceLoader res1 = new SolrResourceLoader(instanceDir1 );
		final ResourceLoader res2 = new SolrResourceLoader(instanceDir2 );
		Executor exec = Executors.newFixedThreadPool(threadPoolCount);
		final CountDownLatch latch=new CountDownLatch(threadCount);
		Runnable command = new Runnable() {
			Configuration singletonConf1 = null;
			Configuration singletonConf2 = null;
			Dictionary singletonDic1 = null;
			Dictionary singletonDic2 = null;
			
			@Override
			public void run() {
//				testInstanceDefaultConfig();
				testInstanceDirectory();
//				testIK();
			}
			
			private void testInstanceDirectory(){
				singletonConf1 = DefaultConfig.getInstance(res1);
				Dictionary.initial(singletonConf1);
				singletonDic1 = Dictionary.getSingleton(res1);
				if (null != Dictionary.getSingleton(res1)&&singletonDic1 != null&&singletonDic1 != Dictionary.getSingleton(res1)) {
					System.out.println("Dictionary1单例验证失败！");
//					System.exit(0);
				}
				singletonConf2 = DefaultConfig.getInstance(res2);
				Dictionary.initial(singletonConf2);
				singletonDic2 = Dictionary.getSingleton(res2);
				if (null != Dictionary.getSingleton(res2)&&singletonDic2 != null&&singletonDic2 != Dictionary.getSingleton(res2)) {
					System.out.println("Dictionary2单例验证失败！");
//					System.exit(0);
				}
				latch.countDown();
//				System.err.println("一个线程执行完毕！");
			}
			
			private void testInstanceDefaultConfig(){
				singletonConf1 = DefaultConfig.getInstance(res1);
				if (singletonConf1 != DefaultConfig.getInstance(res1)) {
					System.out.println("DefaultConfig单例验证失败！");
					System.exit(0);
				}
				latch.countDown();
			}
			
			private void testIK(){
				IKQueryExpressionParser parser = new IKQueryExpressionParser();
				//String ikQueryExp = "newsTitle:'的两款《魔兽世界》插件Bigfoot和月光宝盒'";
				String ikQueryExp = "(id='ABcdRf' && date:{'20010101','20110101'} && keyword:'魔兽中国') || (content:'KSHT-KSH-A001-18'  || ulr='www.ik.com') - name:'林良益'";
				Query result = parser.parseExp(ikQueryExp , true);
				System.out.println(result);		
			}
		};
		for (int i = 0; i < threadCount; i++) {
//			System.err.println("开启线程"+i+1);
			exec.execute(command );
		}
		try {
//			Thread.sleep(c*1000);
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.err.println("主程序退出！");
		System.exit(0);

	}

}
