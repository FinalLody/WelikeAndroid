package com.lody.welike.http;

/**
 * @author Lody
 * @version 1.0
 */
public abstract class HttpConfigFactory {

    /**
     * @return 默认Http配置
     */
    public abstract HttpConfig newDefaultConfig();


    public static final class DefaultHttpConfigFactory extends HttpConfigFactory {

        @Override
        public HttpConfig newDefaultConfig() {
            return new HttpConfig();
        }
    }
}
