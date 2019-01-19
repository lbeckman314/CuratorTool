/*
 * Created on Apr 3, 2008
 *
 */
package org.gk.qualityCheck;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;

/**
 * This class is used to check EntitySetCompartment. This is a very simple check
 * to make sure EntitySet and its members have the same compartment setting.
 * @author wgm
 *
 */
public class EntitySetCompartmentCheck extends CompartmentCheck {
    
    public EntitySetCompartmentCheck() {        
        checkClsName = ReactomeJavaConstants.EntitySet;
        followAttributes = new String[] {
                ReactomeJavaConstants.hasMember,
                ReactomeJavaConstants.hasCandidate
        };
    }
    
    @Override
    public String getDisplayName() {
        return "Extra_Compartments_In_Entity_Set_Or_Members";
    }
    
    @Override
    protected String getIssueTitle() {
        return "Extra_Compartment_DisplayNames";
    }
    
    @Override
    protected String getIssue(GKInstance container) throws Exception {
        Set<GKInstance> contained = getAllContainedEntities(container);
        Set<GKInstance> containedCompartments = getContainedCompartments(contained);
        if (containedCompartments.size() > 2)
            return "More than two compartments in members";
        List<GKInstance> containerCompartments = container.getAttributeValuesList(ReactomeJavaConstants.compartment);
        Set<GKInstance> shared = new HashSet<GKInstance>(containedCompartments);
        shared.retainAll(containerCompartments);
        containedCompartments.removeAll(shared);
        containerCompartments.removeAll(shared);
        StringBuilder builder = new StringBuilder();
        if (containerCompartments.size() > 0) {
            builder.append("EntitySet:");
            containerCompartments.forEach(c -> builder.append(c.getDisplayName()).append(","));
            builder.deleteCharAt(builder.length() - 1);
        }
        if (containedCompartments.size() > 0) {
            if (builder.length() > 0)
                builder.append("; ");
            builder.append("Members:");
            containedCompartments.forEach(c -> builder.append(c.getDisplayName()).append(","));
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
    
    protected void loadAttributes(Collection<GKInstance> instances) throws Exception {
        MySQLAdaptor dba = (MySQLAdaptor) dataSource;
        Set<GKInstance> toBeLoaded = loadEntitySetMembers(instances, dba);
        if (progressPane != null)
            progressPane.setText("Load PhysicalEntity compartment...");
        loadAttributes(toBeLoaded,
                       ReactomeJavaConstants.PhysicalEntity, 
                       ReactomeJavaConstants.compartment,
                       dba);
    }
    
    @Override
    protected Set<GKInstance> filterInstancesForProject(Set<GKInstance> instances) {
        return filterInstancesForProject(instances, ReactomeJavaConstants.EntitySet);
    }
    
    /**
     * For EntitySet, compartments used by members should be the same as
     * its container, EntitySet instance.
     * @param containedCompartments
     * @param containerCompartments
     * @return
     */
    protected boolean compareCompartments(Set containedCompartments,
                                          List containerCompartments) throws Exception {
        if (containedCompartments.size() > 2)
            return false;
        // Components and complex should have the same numbers of compartments used.
        if (containerCompartments.size() != containedCompartments.size())
            return false;
        for (Iterator it = containedCompartments.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (!containerCompartments.contains(obj)) // Make sure these two collections are the same.
                return false;
        }
        // It is OK if nothing has been assigned. The mandatory checking should
        // handle this case
        return true;
    }
    
    protected void grepCheckOutInstances(GKInstance complex,
                                         Set checkOutInstances) throws Exception {
        Set components = getAllContainedEntities(complex);
        checkOutInstances.addAll(components);
    }
    
    protected ResultTableModel getResultTableModel() {
        ResultTableModel tableModel = new ComponentTableModel();
        tableModel.setColNames(new String[] {"Member", "Compartment"});
        return tableModel;
    }
    

    
}
