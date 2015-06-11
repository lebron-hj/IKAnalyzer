package org.wltea.analyzer.solr;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.wltea.analyzer.lucene.IKTokenizer;

public class IKTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware {

	private boolean isMaxWordLength = false;
	private ResourceLoader resourceLoader;
	
	public IKTokenizerFactory(Map<String, String> args) {
		super(args);
		String _arg = (String)args.get("isMaxWordLength");
	    this.isMaxWordLength = _arg==null?false:Boolean.parseBoolean(_arg);
	}
	
	@Override
	public Tokenizer create(AttributeFactory arg0, Reader reader) {
		return new IKTokenizer(reader, isMaxWordLength(),this.resourceLoader);
	}
	
	  public void setMaxWordLength(boolean isMaxWordLength) {
		    this.isMaxWordLength = isMaxWordLength;
		  }

		  public boolean isMaxWordLength() {
		    return this.isMaxWordLength;
		  }

		@Override
		public void inform(ResourceLoader resourceloader) throws IOException {
			this.resourceLoader = resourceloader;
		}

}
