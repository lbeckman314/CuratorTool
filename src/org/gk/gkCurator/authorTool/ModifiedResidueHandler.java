/*
 * Created on Dec 16, 2008
 *
 */
package org.gk.gkCurator.authorTool;

import java.util.Map;
import org.gk.database.AttributeEditConfig;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.render.NodeAttachment;
import org.gk.render.RenderableFeature;

public class ModifiedResidueHandler {
    
    public ModifiedResidueHandler() {
        
    }
    
    /**
     * Returns the feature representing the given residue.
     * 
     * The feature label is determined as follows:
     * <ul>
     * <li>If the residue has a <em>modification</em> value which maps
     *     to a {@link AttributeEditConfig#getModifications()} short name,
     *     then that is the label.
     * </ul>
     * <li>Otherwise, if the residue has a <em>psiMod</em> value and the
     *     parsed residue display name contains a
     *     {@link AttributeEditConfig#getPsiModifications()} short name,
     *     then that is the label.
     * </ul>
     * <li>Otherwise, the label is null.
     * </ul>
     * 
     * <em>Note</em>: this method catches exceptions and prints the
     * exception to <code>stdout</code>. In that case, the returned
     * feature might be incomplete and invalid. There is no definitive
     * way for the caller to detect that situation.
     * 
     * @param modifiedResidue
     * @return the feature
     * @throws Exception
     */
   public RenderableFeature convertModifiedResidue(GKInstance modifiedResidue) {
        // Need to convert to attachments
        Map<String, String> residues = AttributeEditConfig.getConfig().getModificationResidues();
        Map<String, String> modifications = AttributeEditConfig.getConfig().getModifications();
        RenderableFeature feature = new RenderableFeature();
        feature.setReactomeId(modifiedResidue.getDBID());
        try {
            boolean isDone = false;
            if (modifiedResidue.getSchemClass().isValidAttribute(ReactomeJavaConstants.residue)) {
                GKInstance residue = (GKInstance) modifiedResidue.getAttributeValue(ReactomeJavaConstants.residue);
                if (residue != null) {
                    String shortName = residues.get(residue.getDisplayName());
                    if (shortName != null)
                        feature.setResidue(shortName);
                }
                isDone = true;
            }
            if (modifiedResidue.getSchemClass().isValidAttribute(ReactomeJavaConstants.modification)) {
                GKInstance modification = (GKInstance) modifiedResidue.getAttributeValue(ReactomeJavaConstants.modification);
                if (modification != null) {
                    String shortName = modifications.get(modification.getDisplayName());
                    if (shortName != null)
                        feature.setLabel(shortName);
                }
                isDone = true;
            }
            // Need to check if psiMod has been assigned
            if (!isDone && modifiedResidue.getSchemClass().isValidAttribute(ReactomeJavaConstants.psiMod)) {
                String displayName = modifiedResidue.getDisplayName();
                // Need to do some parse
                String[] tokens = displayName.split("(-| )");
                String psiResidue = searchPsiResidue(tokens);
                feature.setResidue(psiResidue);
                String psiModification = searchPsiModification(tokens);
                feature.setLabel(psiModification);
            }
        }
        catch(Exception e) {
            System.err.println("ModifiedResidueHandler.convertModifiedResidue(): " + e);
            e.printStackTrace();
        }
        // Assign a random position
        setRandomPosition(feature);
        return feature;
    }
    
    private String searchPsiResidue(String[] tokens) {
        Map<String, String> psiModificationResidues = AttributeEditConfig.getConfig().getPsiModificationResidues();
        if (psiModificationResidues == null)
            return null;
        for (String token : tokens) {
            String tmp = psiModificationResidues.get(token);
            if (tmp != null)
                return tmp;
        }
        return null;
    }
    
    private String searchPsiModification(String[] tokens) {
        Map<String, String> psiModifications = AttributeEditConfig.getConfig().getPsiModifications();
        for (String token : tokens) {
            String tmp = psiModifications.get(token);
            if (tmp != null)
                return tmp;
        }
        return null;
    }
    
    private void setRandomPosition(NodeAttachment attachment) {
        double x = Math.random();
        double y = Math.random();
        // Check if it should be in x or y
        double tmp = Math.random();
        if (tmp < 0.25)
            x = 0.0;
        else if (tmp < 0.50)
            x = 1.0;
        else if (tmp < 0.57)
            y = 0.0;
        else 
            y = 1.0;
        attachment.setRelativePosition(x, y);
    }
}
