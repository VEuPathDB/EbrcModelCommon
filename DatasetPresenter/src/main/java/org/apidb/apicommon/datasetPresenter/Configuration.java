package org.apidb.apicommon.datasetPresenter;

import org.gusdb.fgputil.xml.Text;

public class Configuration {
  private String username;
  private String password;
  
  public void setUsername(Text username) {
    this.username = username.getText();
  }
  
  public void setPassword(Text password) {
    this.password = password.getText();
  }
  
  public String getUsername() { return username; }
  
  public String getPassword() { return password; }

}
