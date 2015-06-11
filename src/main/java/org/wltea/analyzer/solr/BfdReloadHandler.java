package org.wltea.analyzer.solr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.cfg.Configuration;

/**
 * 将master的词典文件同步到本slave
 */
public class BfdReloadHandler implements SolrRequestHandler, SolrCoreAware {

	protected static Logger log = LoggerFactory
			.getLogger(BfdReloadHandler.class);
	private static final String bfdAction = "action";
	private static final String SYNC_FILES = "SYNC_FILES";

	@Override
	public Category getCategory() {
		return Category.OTHER;
	}

	@Override
	public String getDescription() {
		return "Register Standard Bfd Custom Handlers";
	}

	@Override
	public URL[] getDocs() {
		return null;
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public String getSource() {
		return "$URL: https://***";
	}

	@Override
	public NamedList getStatistics() {
		return null;
	}

	@Override
	public String getVersion() {
		return getClass().getPackage().getSpecificationVersion();
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse resp) {
		SolrParams params = req.getParams();
		String action = params.get(BfdReloadHandler.bfdAction);
		if (SYNC_FILES.equals(action)) {
			for (String file : urls.keySet()) {
				refreshFile(file, urls.get(file), resp);
			}
		}
	}

	private void refreshFile(String resource, String url, SolrQueryResponse resp) {
		InputStream in = null;
		try {
			log.warn(url + "start download.");
			HttpURLConnection conn = (HttpURLConnection) new URL(url + "?_="
					+ System.currentTimeMillis()).openConnection();
			conn.setRequestProperty("content-type", "binary/data");
			conn.setConnectTimeout(15 * 1000);
			conn.setReadTimeout(60 * 1000);
			// 设置 HttpURLConnection的字符编码
	        conn.setRequestProperty("Accept-Charset", "UTF-8");
			in = conn.getInputStream();
			File file = new File((new StringBuilder()).append(solrConfig)
					.append(resource).toString());
			if (file != null) {
				if (!file.exists()) {
					file.createNewFile();
				}
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
				InputStreamReader reader = new InputStreamReader(in, "utf-8");
				BufferedReader br = new BufferedReader(reader);
				String line = null;
				while ((line = br.readLine()) != null) {
					writer.write(line+"\n");
				}
				writer.close();
				log.warn(resource+file.getUsableSpace());
			}
			log.info(url + "sync success.");
			resp.add(resource, "true");
		} catch (IOException e) {
			log.warn(url + "sync fail.");
			resp.add(resource, "false");
			e.printStackTrace();
		} finally {
			if (in != null) {
				IOUtils.closeQuietly(in);
			}
		}
	}

	@Override
	public void init(NamedList namedlist) {
		NamedList initArgs = namedlist;
		urls = new HashMap<String, String>();
		if (initArgs.get(Configuration.DICT_FILE) != null) {
			urls.put(Configuration.DICT_FILE,
					initArgs.get(Configuration.DICT_FILE).toString());
		}
		if (initArgs.get(Configuration.STOP_FILE) != null) {
			urls.put(Configuration.STOP_FILE,
					initArgs.get(Configuration.STOP_FILE).toString());
		}
		if (initArgs.get(Configuration.QUANTIFIER_FILE) != null) {
			urls.put(Configuration.QUANTIFIER_FILE,
					initArgs.get(Configuration.QUANTIFIER_FILE).toString());
		}
		if (initArgs.get(Configuration.SYNONYM_FILE) != null) {
			urls.put(Configuration.SYNONYM_FILE,
					initArgs.get(Configuration.SYNONYM_FILE).toString());
		}
		
	}

	String solrConfig;
	Map<String, String> urls;

	@Override
	public void inform(SolrCore solrcore) {
		solrConfig = solrcore.getResourceLoader().getConfigDir();
		String path = null;
		for (Map.Entry<String, SolrRequestHandler> entry : solrcore
				.getRequestHandlers().entrySet()) {
			if (entry.getValue() == this) {
				path = entry.getKey();
				break;
			}
		}
		if (path == null) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"The BfdReloadHandler is not registered with the current core.");
		}
		if (!path.startsWith("/")) {
			throw new SolrException(
					SolrException.ErrorCode.SERVER_ERROR,
					"The BfdReloadHandler needs to be registered to a path.  Typically this is '/bfd'");
		}
	}
}
