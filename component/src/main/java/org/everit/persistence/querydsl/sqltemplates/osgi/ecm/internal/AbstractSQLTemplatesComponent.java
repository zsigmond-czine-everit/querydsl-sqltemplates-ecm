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

import java.util.Dictionary;
import java.util.Hashtable;

import org.everit.osgi.ecm.annotation.Deactivate;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttribute;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttributes;
import org.everit.osgi.ecm.annotation.attribute.CharacterAttribute;
import org.everit.osgi.ecm.annotation.attribute.CharacterAttributes;
import org.everit.osgi.ecm.component.ComponentContext;
import org.everit.persistence.querydsl.sqltemplates.osgi.ecm.PriorityConstants;
import org.everit.persistence.querydsl.sqltemplates.osgi.ecm.SQLTemplatesConstants;
import org.osgi.framework.ServiceRegistration;

import com.querydsl.sql.SQLTemplates;

/**
 * Abstract SQL Templates component information and process.
 */
@BooleanAttributes({
    @BooleanAttribute(attributeId = SQLTemplatesConstants.ATTR_NEWLINETOSINGLESPACE,
        defaultValue = false, priority = PriorityConstants.PRIORITY_07,
        label = "New line to single space",
        description = "Replaces new line characters with a single space."),
    @BooleanAttribute(attributeId = SQLTemplatesConstants.ATTR_QUOTE, defaultValue = false,
        priority = PriorityConstants.PRIORITY_06, label = "Quote",
        description = "This property determines whether or not the SQLTemplate will be quoting in "
            + "SQL strings."),
    @BooleanAttribute(attributeId = SQLTemplatesConstants.ATTR_PRINTSCHEMA, defaultValue = false,
        priority = PriorityConstants.PRIORITY_05, label = "Printschema",
        description = "This property determines whether or not the SQLTemplate will insert the "
            + "schema name before the table name in the SQL expressions. "
            + "/select * from schemaname.tablename;/.") })
@CharacterAttributes({
    @CharacterAttribute(attributeId = SQLTemplatesConstants.ATTR_ESCAPE, defaultValue = '\\',
        priority = PriorityConstants.PRIORITY_08, label = "Escape character",
        description = "Escape character.") })
public abstract class AbstractSQLTemplatesComponent {

  /**
   * SQLTemplates OSGi service registration instance.
   */
  private ServiceRegistration<?> serviceRegistration;

  /**
   * Component deactivate method.
   */
  @Deactivate
  public void deactivate() {
    if (serviceRegistration != null) {
      serviceRegistration.unregister();
    }
  }

  protected abstract SQLTemplates getSQLTemplates();

  /**
   * Component activator method.
   */
  protected void registerService(final ComponentContext<?> componentContext) {
    Dictionary<String, Object> properties =
        new Hashtable<String, Object>(componentContext.getProperties());
    serviceRegistration =
        componentContext.registerService(SQLTemplates.class, getSQLTemplates(), properties);
  }
}
