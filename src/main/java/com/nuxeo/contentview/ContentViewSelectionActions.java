/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Anahide Tchertchian
 */

package com.nuxeo.contentview;

import static org.jboss.seam.ScopeType.EVENT;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.actions.ActionContext;
import org.nuxeo.ecm.platform.actions.ejb.ActionManager;
import org.nuxeo.ecm.platform.contentview.jsf.ContentView;
import org.nuxeo.ecm.platform.query.api.PageSelection;
import org.nuxeo.ecm.platform.query.api.PageSelections;
import org.nuxeo.ecm.webapp.action.ActionContextProvider;
import org.nuxeo.ecm.webapp.clipboard.ClipboardActions;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;

/**
 * Handles selection from given content view selected elements, and use standard content view selection actions after
 * that.
 */
@Name("contentViewSelectionActions")
@Scope(EVENT)
public class ContentViewSelectionActions {

    @In(required = false, create = true)
    protected transient DocumentsListsManager documentsListsManager;

    @In(required = false, create = true)
    protected ClipboardActions clipboardActions;

    @In(required = false, create = true)
    protected transient ActionManager actionManager;

    @In(create = true, required = false)
    protected transient ActionContextProvider actionContextProvider;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    protected List<DocumentModel> getSelectedDocuments(ContentView cv) {
        List<DocumentModel> selected = new ArrayList<DocumentModel>();
        PageSelections<?> ps = cv.getCurrentPageProvider().getCurrentSelectPage();
        if (ps != null) {
            for (PageSelection<?> elt : ps.getEntries()) {
                if (elt.isSelected()) {
                    selected.add((DocumentModel) elt.getData());
                }
            }
        }
        return selected;
    }

    protected boolean checkFilter(String filterId, ContentView contentView, List<DocumentModel> selectedEntries) {
        ActionContext ctx = actionContextProvider.createActionContext();
        ctx.putLocalVariable("contentView", contentView);
        ctx.putLocalVariable("selectedDocuments", selectedEntries);

        if (!actionManager.checkFilter(filterId, ctx)) {
            facesMessages.add(StatusMessage.Severity.ERROR, "Cannot perform action with selected item(s)");
            return false;
        }
        return true;
    }

    public void putSelectionInClipboard(ContentView cv, String filterId) {
        if (cv == null) {
            return;
        }
        List<DocumentModel> s = getSelectedDocuments(cv);
        documentsListsManager.addToWorkingList(cv.getSelectionListName(), s);
        if (!checkFilter(filterId, cv, s)) {
            return;
        }
        clipboardActions.putSelectionInClipboard();
    }

}
