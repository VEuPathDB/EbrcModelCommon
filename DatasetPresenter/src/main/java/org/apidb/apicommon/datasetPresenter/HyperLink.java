package org.apidb.apicommon.datasetPresenter;

import org.gusdb.fgputil.xml.Text;

public class HyperLink {
  private String url;
  private String text;
  private String description;
    private String category; // optinally provided by the default Hyperlinks file
    private String isPublication; // optionally add 'yes' to indicate that this is a publication link.

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

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }

  public void setIsPublication(String isPublication) {
    if (isPublication.toLowerCase().equals("yes")) {
      this.isPublication = "Y";
    } else {
      this.isPublication = "N";
    }

  }

  public String getIsPublication() {
    return isPublication;
  } 

  
}
