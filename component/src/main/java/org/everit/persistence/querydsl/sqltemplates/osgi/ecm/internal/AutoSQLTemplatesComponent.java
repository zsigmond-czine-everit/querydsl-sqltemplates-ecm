/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.persistence.querydsl.sqltemplates.osgi.ecm.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.persistence.querydsl.sqltemplates.osgi.ecm.DBMSType;
import org.everit.persistence.querydsl.sqltemplates.osgi.ecm.SQLTemplatesConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentException;
import org.osgi.service.log.LogService;

import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.SQLTemplates.Builder;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Component that automatically detects the type of the database based on the referenced DataSource
 * and registers the right type of SQLTemplates instance.
 */

@Component(componentId = SQLTemplatesConstants.SERVICE_FACTORY_PID_AUTO_SQL_TEMPLATES,
    configurationPolicy = ConfigurationPolicy.FACTORY)
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION,
        defaultValue = SQLTemplatesConstants.DEFAULT_SERVICE_DESCRIPTION_AUTO_SQL_TEMPLATES) })
public class AutoSQLTemplatesComponent extends AbstractSQLTemplatesComponent {

  /**
   * The datasource that is used to find out the type of the database.
   */
  private DataSource dataSource;

  /**
   * The logging service.
   */
  private LogService logService;

  private SQLTemplates sqlTemplates;

  /**
   * Automatically configures an {@link SQLTemplates} instance based on the underlying
   * {@code dataSource} and registers it as an OSGi service.
   *
   * @param context
   *          bundle context
   * @param componentProperties
   *          component properties
   */
  @Override
  @Activate
  public void activate(final BundleContext context, final Map<String, Object> componentProperties) {
    Builder sqlTemplateBuilder = null;
    String dbProductName = "";
    int dbMajorVersion = 0;

    try (Connection conn = dataSource.getConnection()) {
      dbProductName = conn.getMetaData().getDatabaseProductName();
      dbMajorVersion = conn.getMetaData().getDatabaseMajorVersion();
    } catch (SQLException e) {
      throw new ComponentException("Cannot get Database product name of the given DataSource.", e);
    }

    sqlTemplateBuilder = DBMSType.getByProductNameAndMajorVersion(dbProductName, dbMajorVersion)
        .getSQLTemplatesBuilder();

    new SQLTemplateConfigurator(sqlTemplateBuilder, componentProperties).configure();

    sqlTemplates = sqlTemplateBuilder.build();

    componentProperties.put(SQLTemplatesConstants.ATTR_SELECTED_TEMPLATE,
        sqlTemplateBuilder.getClass().getName());
    super.activate(context, componentProperties);

    logService.log(LogService.LOG_INFO,
        "Selected template: " + sqlTemplateBuilder.getClass().getName());
  }

  @Override
  protected SQLTemplates getSQLTemplates() {
    return sqlTemplates;
  }

  @ServiceRef(attributeId = SQLTemplatesConstants.ATTR_DATASOURCE, defaultValue = "")
  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @ServiceRef(attributeId = SQLTemplatesConstants.ATTR_LOGSERVICE, defaultValue = "")
  public void setLogService(final LogService logService) {
    this.logService = logService;
  }

}
