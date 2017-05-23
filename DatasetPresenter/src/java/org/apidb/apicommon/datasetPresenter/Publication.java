package org.apidb.apicommon.datasetPresenter;

public class Publication {
    private String pubmedId;
    private String citation;

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }
    
    public String getPubmedId() {
        return pubmedId;
    }

    public String getCitation() {
        if (pubmedId == null || pubmedId.equals("")) return null;
        if (citation == null) {
            try {
                byte[] bo = new byte[100000];
                byte[] err = new byte[100000];
                String[] cmd = { "pubmedIdToCitation", pubmedId };
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                p.getInputStream().read(bo);
                p.getErrorStream().read(err);
		if (p.exitValue() != 0) {
		  throw new UnexpectedException("Failed running 'pubmedIdToCitation "
						+ pubmedId + "'  --  " + new String(err));
		}
                p.destroy();
                citation = new String(bo, "UTF-8").trim();
            } catch (Exception e) {
                throw new UnexpectedException("Failed running: pubmedIdToCitation "
                                              + pubmedId, e);
            }
	    if (citation == null) throw new UnexpectedException("Failed running: pubmedIdToCitation "
								+ pubmedId + " -- returned null citation");
        }
        return citation;
    }

    @Override
    public String toString() {
        return("PMID: " + this.pubmedId + "\tCitation: " + this.citation);
    }


    
}
