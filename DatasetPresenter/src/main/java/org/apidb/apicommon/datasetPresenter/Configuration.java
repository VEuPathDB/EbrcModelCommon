package org.apidb.apicommon.datasetPresenter;

import org.gusdb.fgputil.xml.Text;

public class Configuration {
  private String username;
  private String password;
  private String schema;
  
  public void setUsername(Text username) {
    this.username = username.getText();
  }

  public void setPassword(Text password) {
    this.password = password.getText();
  }

  public void setSchema(Text schema) { this.schema = schema.getText(); }

  public String getUsername() { return username; }

  public String getPassword() { return password; }

  public String getSchema() { return schema;  }
}
