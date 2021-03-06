/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.artifact.service.artifact.builder.spring;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.identity.artifact.service.exception.BuilderException;
import org.wso2.identity.artifact.service.exception.ClientException;
import org.wso2.identity.artifact.service.exception.ServiceException;

import javax.ws.rs.core.HttpHeaders;

/**
 * This class is used to call DCR endpoint of Identity Server and get application details.
 */
public class DCRClient {

    /**
     * Returns the application details by calling DCR endpoint of Identity Server.
     *
     * @param server      Identity Server name.
     * @param application Application name.
     * @return Application details.
     * @throws BuilderException
     */
    public static JSONObject getApplication(String server, String application) throws BuilderException {

        JSONObject dcrResponse;
        try {
            // Proxy the access-token from the request and make a get request to dcr endpoint of IS using that token.
            HttpHeaders headers = ResteasyProviderFactory.getContextData(HttpHeaders.class);
            String dcrEndpoint = server + SpringBuilderConstants.DCR_ENDPOINT + application;
            URL url = new URL(dcrEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(SpringBuilderConstants.HTTP_GET);
            conn.setRequestProperty(SpringBuilderConstants.HEADER_ACCEPT, SpringBuilderConstants.DATA_ACCEPT_HEADER);
            conn.setRequestProperty(SpringBuilderConstants.Authorization,
                    headers.getHeaderString(SpringBuilderConstants.AUTHORIZATION_HEADER));

            if (conn.getResponseCode() != 200) {
                throw new BuilderException(new ClientException("Obtained " + conn.getResponseCode() + "response from" +
                        " DCR endpoint"));
            }

            // Obtain the json response from dcr endpoint.
            JSONParser jsonParser = new JSONParser();
            dcrResponse = (JSONObject) jsonParser.parse(new InputStreamReader((conn.getInputStream())));
            conn.disconnect();

        } catch (ParseException e) {
            throw new BuilderException(new ServiceException("Error while passing the response from dcr endpoint"));
        } catch (IOException e) {
            throw new BuilderException(new ClientException("DCR endpoint that is not valid"));
        }
        return dcrResponse;
    }
}
