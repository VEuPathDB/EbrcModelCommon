package org.apidb.apicommon.datasetPresenter;

import org.gusdb.fgputil.xml.Text;

public class HyperLink {
  private String url;
  private String text;
  private String description;
    private String type; // optinally provided by the default Hyperlinks file
    private String subtype; // optinally provided by the default Hyperlinks file

  public void setUrl(Text url) {
    this.url = url.getText();
  }
  
  public void setText(Text text) {
    this.text = text.getText();
  }

  public void setDescription(Text description) {
    this.description = description.getText();
  }
  
  public String getUrl() {
    return url;
  }
  
  public String getText() {
    return text;
  }

  public String getDescription() {
    return description;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public String getSubtype() {
    return subtype;
  }

  
}
