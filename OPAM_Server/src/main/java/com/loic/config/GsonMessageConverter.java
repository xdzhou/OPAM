package com.loic.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class GsonMessageConverter extends AbstractHttpMessageConverter<Object>
		implements GenericHttpMessageConverter<Object> {
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	private Gson _gson;
	private Type _type = null;
	private boolean _prefixJson = false;

	/**
	 * Construct a new {@code GsonMessageConverter} with a default
	 * {@link Gson#Gson() Gson}.
	 */
	public GsonMessageConverter() {
		this(new Gson());
	}

	/**
	 * Construct a new {@code GsonMessageConverter}.
	 * 
	 * @param serializeNulls
	 *            true to generate json for null values
	 */
	public GsonMessageConverter(boolean serializeNulls) {
		this(serializeNulls ? new GsonBuilder().serializeNulls().create()
				: new Gson());
	}

	/**
	 * Construct a new {@code GsonMessageConverter}.
	 * 
	 * @param gson
	 *            a customized {@link Gson#Gson() Gson}
	 */
	public GsonMessageConverter(Gson gson) {
		super(new MediaType("application", "json", DEFAULT_CHARSET));
		setGson(gson);
	}

	/**
	 * Sets the {@code Gson} for this view. If not set, a default
	 * {@link Gson#Gson() Gson} is used.
	 * <p>
	 * Setting a custom-configured {@code Gson} is one way to take further
	 * control of the JSON serialization process.
	 * 
	 * @throws IllegalArgumentException
	 *             if gson is null
	 */
	public void setGson(Gson gson) {
		Assert.notNull(gson, "'gson' must not be null");
		_gson = gson;
	}

	public void setType(Type type) {
		this._type = type;
	}

	public Type getType() {
		return _type;
	}

	/**
	 * Indicates whether the JSON output by this view should be prefixed with
	 * "{} &&". Default is false.
	 * <p>
	 * Prefixing the JSON string in this manner is used to help prevent JSON
	 * Hijacking. The prefix renders the string syntactically invalid as a
	 * script so that it cannot be hijacked. This prefix does not affect the
	 * evaluation of JSON, but if JSON validation is performed on the string,
	 * the prefix would need to be ignored.
	 */
	public void setPrefixJson(boolean prefixJson) {
		this._prefixJson = prefixJson;
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return canRead(mediaType);
	}

	@Override
	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
		return canRead(mediaType);
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return canWrite(mediaType);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		// should not be called, since we override canRead/Write instead
		throw new UnsupportedOperationException();
	}

	/**
	 * Generic types will be processed here.
	 */
	@Override
	public Object read(Type type, Class<?> contextClass,
			HttpInputMessage inputMessage)
			throws HttpMessageNotReadableException, IOException {
		setType(type);
		return readInternal(null, inputMessage);
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		Reader json = new InputStreamReader(inputMessage.getBody(),
				getCharset(inputMessage.getHeaders()));

		try {
			Type typeOfT = getType();
			if (typeOfT != null) {
				return _gson.fromJson(json, typeOfT);
			} else {
				return _gson.fromJson(json, clazz);
			}
		} catch (JsonSyntaxException ex) {
			throw new HttpMessageNotReadableException("Could not read JSON: "
					+ ex.getMessage(), ex);
		} catch (JsonIOException ex) {
			throw new HttpMessageNotReadableException("Could not read JSON: "
					+ ex.getMessage(), ex);
		} catch (JsonParseException ex) {
			throw new HttpMessageNotReadableException("Could not read JSON: "
					+ ex.getMessage(), ex);
		} finally {
			setType(null);
		}
	}

	@Override
	protected void writeInternal(Object o, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		OutputStreamWriter writer = new OutputStreamWriter(
				outputMessage.getBody(), getCharset(outputMessage.getHeaders()));

		try {
			if (this._prefixJson) {
				writer.append("{} && ");
			}
			Type typeOfSrc = getType();
			if (typeOfSrc != null) {
				_gson.toJson(o, typeOfSrc, writer);
			} else {
				_gson.toJson(o, writer);
			}
			writer.close();
		} catch (JsonIOException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: "
					+ ex.getMessage(), ex);
		}
	}

	// helpers

	private Charset getCharset(HttpHeaders headers) {
		if (headers != null && headers.getContentType() != null
				&& headers.getContentType().getCharSet() != null) {
			return headers.getContentType().getCharSet();
		}
		return DEFAULT_CHARSET;
	}

}