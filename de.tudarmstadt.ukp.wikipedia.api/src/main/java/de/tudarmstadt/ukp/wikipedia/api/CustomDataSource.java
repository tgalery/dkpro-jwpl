/*******************************************************************************
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import org.slf4j.Logger;

import java.sql.*;

public class CustomDataSource extends DatabaseConfiguration {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CustomDataSource.class);

    private String jdbcURL;
    private String databaseDriver;

    /*
     * needed to please frameworks like Spring... parameter injection is done
     * via setters there
     */
    public CustomDataSource() {
        super();
    }

    public CustomDataSource(String hostName, String dbName, String user, String password, String driverClassName, boolean useSSL)  {
        this();
        setDbName(dbName);
        setHostName(hostName);
        setPassword(password);
        setUserName(user);
        // check if the DB driver is available in the classpath
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
        String baseJdbcURL = "jdbc:mysql://" + getHostName() + "/" + getDbName();
        if(!hasExternalSSLParams(baseJdbcURL)) {
            if (useSSL) {
                setJdbcURL(baseJdbcURL + "?verifyServerCertificate=false&amp;useSSL=true");
            } else {
                setJdbcURL(baseJdbcURL + "?useSSL=false");
            }
        } else {
            setJdbcURL(baseJdbcURL);
        }

        Language lang = requestWikiLangFromDB(hostName, dbName, user, password);
        setLanguage(lang);
    }

    private boolean hasExternalSSLParams(String baseJdbcURL) {
        return baseJdbcURL.contains("useSSL=");
    }

    /*
     * Although the JWPL-DataBase knows it's Wikipedia language (described as
     * <code>language</code> in the table <code>MetaData</code>), the
     * {@link DatabaseConfiguration} needs to know the specified
     * {@link Language}. Hence, it will be requested by this method so the user
     * does not have to configure the {@link Language} manually.
     *
     * @param hostName
     * @param dbName
     * @param user
     * @param password
     * @return the language found in the <code>MetaData</code>-table, as
     * enumeration instance of {@link Language}
     * @throws WikiServiceException
     */
    private Language requestWikiLangFromDB(String hostName, String dbName, String user, String password)  {

        try (Connection connection = DriverManager.getConnection(getJdbcURL(), user, password)){

            Statement stmnt = connection.createStatement();
            ResultSet result = stmnt.executeQuery("Select language from MetaData");
            if (result.next()) {
                String languageString = result.getString(1);

                logger.info("The language found at {}:{} is '{}' and will be set to this Wiki-DB connection", hostName, dbName, languageString);
                if (languageString.equals("türkçe")) {
                    languageString = "turkish";
                }
                return WikiConstants.Language.valueOf(languageString);
            } else {
                throw new RuntimeException("No language could be found for this Wikipedia DB. This is very strange, check your DB setup!");
            }

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public void setDbName(String dbName) {
        assert dbName!=null;
        assert dbName.trim().length() > 0;

        super.setDatabase(dbName);
    }

    public String getDbName() {
        return super.getDatabase();
    }

    public void setHostName(String hostName) {
        assert hostName!=null;
        assert hostName.trim().length() > 0;

        super.setHost(hostName);
    }

    public String getHostName() {
        return super.getHost();
    }

    public String getUserName() {
        return super.getUser();
    }

    public void setUserName(String user) {
        assert user!=null;
        assert user.trim().length() > 0;
        super.setUser(user);
    }

    /**
     * @param databaseDriver the databaseDriver to set
     */
    public void setDatabaseDriver(String databaseDriver) {
        assert databaseDriver!=null;
        assert databaseDriver.trim().length() > 0;
        this.databaseDriver = databaseDriver;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    /**
     * @param jdbcURL the jdbcURL to set
     */
    public void setJdbcURL(String jdbcURL) {
        assert jdbcURL!=null;
        assert jdbcURL.trim().length() > 0;
        this.jdbcURL = jdbcURL;
    }

    public String getJdbcURL() {
        return jdbcURL;
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }

    @Override
    public WikiConstants.Language getLanguage() {
        return super.getLanguage();
    }

    @Override
    public void setLanguage(WikiConstants.Language language) {
        assert language != null;

        super.setLanguage(language);
    }

}