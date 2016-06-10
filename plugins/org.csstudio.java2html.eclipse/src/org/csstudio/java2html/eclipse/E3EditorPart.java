package org.csstudio.java2html.eclipse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.e4.tools.compat.parts.DIEditorPart;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class E3EditorPart extends DIEditorPart<HTMLJEditorPart> {
    private static final Logger LOG = Logger.getLogger(HtmlComponent.class.getName());

    /** Edit ID as defined by the editor registered in plugin.xml */
    public static final String EDITOR_ID = "org.csstudio.java2html.eclipse.Editor";

    public static E3EditorPart createInstance(IEditorInput input) {
        try {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            final IWorkbenchPage page = window.getActivePage();
            return (E3EditorPart) page.openEditor(input, EDITOR_ID);
        } catch (PartInitException e) {
            LOG.log(Level.WARNING, "Can't load E3Part");
            return null;
        }
    }

    public E3EditorPart() {
        super(HTMLJEditorPart.class);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        getContext().declareModifiable(IEditorSite.class);
        getContext().declareModifiable(MUILabel.class);
        getContext().set(IEditorSite.class, getEditorSite());
        getContext().set(MUILabel.class, new CompatLabel());
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        // Put component in context for use in handlers. //
        getContext().declareModifiable(HTMLJEditorPart.class);
        getContext().set(HTMLJEditorPart.class, getComponent());
    }

    /**
     * Compatibility class to provided a partial E4 implementation.
     */
    protected class CompatLabel implements MUILabel {

        @Override
        public void updateLocalization() {
            return;
        }

        @Override
        public String getIconURI() {
            return null;
        }

        @Override
        public void setIconURI(String value) {
            return;
        }

        @Override
        public String getLabel() {
            return E3EditorPart.this.getPartName();
        }

        @Override
        public String getLocalizedLabel() {
            return getLabel();
        }

        @Override
        public void setLabel(String value) {
            E3EditorPart.this.setPartName(value);
        }

        @Override
        public String getTooltip() {
            return E3EditorPart.this.getTitleToolTip();
        }

        @Override
        public String getLocalizedTooltip() {
            return getTitleToolTip();
        }

        @Override
        public void setTooltip(String value) {
            E3EditorPart.this.setTitleToolTip(value);
        }
    }
}
