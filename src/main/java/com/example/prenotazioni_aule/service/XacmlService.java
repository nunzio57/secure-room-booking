package com.example.prenotazioni_aule.service;

import org.springframework.stereotype.Service;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import jakarta.annotation.PostConstruct;
import java.io.File;

@Service
public class XacmlService {

    private PDP pdp;

    @PostConstruct
    public void init() {
        try {
            // USIAMO IL PERCORSO ASSOLUTO DEL TUO PC
            // Copia questa stringa esattamente com'è:
            String policyPath = "C:\\Users\\Nunzio\\OneDrive\\Desktop\\progetto_ss\\prenotazioni-aule\\prenotazioni-aule\\src\\main\\resources\\policies";

            File policyDir = new File(policyPath);

            // --- DEBUG: VEDIAMO SE TROVA I FILE ---
            System.out.println("🔍 XACML: Sto guardando in: " + policyPath);

            if (policyDir.exists()) {
                File[] files = policyDir.listFiles();
                if (files != null) {
                    System.out.println("📄 File trovati nella cartella: " + files.length);
                    for (File f : files) {
                        System.out.println("   -> " + f.getName());
                    }
                }
            } else {
                System.err.println("⛔ ERRORE: La cartella non esiste! Controlla il percorso.");
            }
            // --------------------------------------

            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyPath);

            Balana balana = Balana.getInstance();
            PDPConfig pdpConfig = balana.getPdpConfig();
            this.pdp = new PDP(pdpConfig);

            System.out.println("✅ Motore XACML avviato.");
        } catch (Exception e) {
            System.err.println("❌ Errore avvio XACML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean evaluate(String role, String resource, String action) {
        String request =
                "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                        "   \n" +
                        "   <Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                        "      <Attribute AttributeId=\"urn:role\" IncludeInResult=\"false\">\n" +
                        "         <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + role + "</AttributeValue>\n" +
                        "      </Attribute>\n" +
                        "   </Attributes>\n" +
                        "   \n" +
                        "   <Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                        "      <Attribute AttributeId=\"urn:action\" IncludeInResult=\"false\">\n" +
                        "         <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + action + "</AttributeValue>\n" +
                        "      </Attribute>\n" +
                        "   </Attributes>\n" +
                        "</Request>";

        String response = pdp.evaluate(request);

        // DEBUG: Vediamo cosa risponde
        //System.out.println(" RAW XACML RESPONSE:\n" + response);
        return response.contains("<Decision>Permit</Decision>");
    }
}