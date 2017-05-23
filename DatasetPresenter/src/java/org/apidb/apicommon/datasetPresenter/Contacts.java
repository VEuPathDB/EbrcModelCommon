package org.apidb.apicommon.datasetPresenter;

import java.util.HashMap;
import java.util.Map;

public class Contacts {
  
  Map<String,Contact> contacts = new HashMap<String,Contact>();
  String xmlFileName;
  
  public void addContact(Contact contact) {
    contacts.put(contact.getId(), contact);
  }
  
  Contact get(String contactId) {
    return contacts.get(contactId);
  }
  
  void setContactsFileName(String contactsFileName) {
    xmlFileName = contactsFileName;
  }
  
  String getContactsFileName() {
    return xmlFileName;
  }

}
