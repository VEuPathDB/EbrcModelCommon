package org.apidb.apicommon.datasetPresenter;

import org.gusdb.fgputil.xml.Text;

public class Contact implements Cloneable {
  private String name;
  private boolean isPrimary = false;
  private String email;
  private String institution;
  private String address;
  private String city;
  private String state;
  private String country;
  private String zip;
  private String id;

  @Override
  public Object clone() { 
    try { 
      return super.clone(); 
    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  public void setContactId(Text id) {
    this.id  = id.getText();
  }
  
  public void setName(Text name) {
    this.name  = name.getText();
  }
  
  public void setIsPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
  }
  
  public void setEmail(Text email) {
    this.email  = email.getText();
  }
  
  public void setInstitution(Text institution) {
    this.institution  = institution.getText();
  }
  
  public void setAddress(Text address) {
    this.address  = address.getText();
  }
  
  public void setCity(Text city) {
    this.city  = city.getText();
  }
  
  public void setState(Text state) {
    this.state  = state.getText();
  }
  
  public void setCountry(Text country) {
    this.country  = country.getText();
  }
  
  public void setZip(Text zip) {
    this.zip  = zip.getText();
  }
  
  public String getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean getIsPrimary() {
    return isPrimary;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getInstitution() {
    return institution;
  }
  
  public String getAddress() {
    return address;
  }
  
  public String getCity() {
    return city;
  }
  
  public String getState() {
    return state;
  }
  
  public String getCountry() {
    return country;
  }
  
  public String getZip() {
    return zip;
  }
  
  
}
