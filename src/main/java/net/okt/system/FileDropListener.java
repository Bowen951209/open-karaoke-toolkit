package net.okt.system;

import net.okt.gui.LineNumberedScrollableTextArea;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.Objects;

// Modified from https://www.specialagentsqueaky.com/blog-post/mbu5p27a/2011-01-09-drag-and-dropping-files-to-java-desktop-application/
public class FileDropListener implements DropTargetListener {
    private final SaveLoadManager saveLoadManager;
    private final LineNumberedScrollableTextArea textArea;

    public FileDropListener(SaveLoadManager saveLoadManager, LineNumberedScrollableTextArea textArea) {
        this.saveLoadManager = saveLoadManager;
        this.textArea = textArea;
    }

    /**
     * @param transferable The transfer which can provide the dropped item data.
     * @return A {@link List} of {@link File}.
     */
    private static List<?> getFiles(Transferable transferable) {
        // Get the data formats of the dropped item.
        DataFlavor[] flavors = transferable.getTransferDataFlavors();

        List<?> files = null;

        // Loop through the flavors.
        for (DataFlavor flavor : flavors) {
            try {
                // If the drop items are files.
                if (flavor.isFlavorJavaFileListType()) {
                    // Get all the dropped files.
                    files = (List<?>) transferable.getTransferData(flavor);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return files;
    }

    @Override
    public void dragEnter(DropTargetDragEvent e) {
        // Accept copy drops.
        e.acceptDrag(DnDConstants.ACTION_COPY);

        List<?> files = getFiles(e.getTransferable());

        // Check if one of the files is loadable. If no file is accepted by OKT, a reject cursor will show up.
        for (Object o : files) {
            File file = (File) o;

            if (!FileExtensionUtils.isLoadable(file)) {
                e.rejectDrag();
                return;
            }
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent e) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent e) {
    }

    @Override
    public void dragExit(DropTargetEvent e) {
    }

    @Override
    public void drop(DropTargetDropEvent e) {
        // Accept copy drops.
        e.acceptDrop(DnDConstants.ACTION_COPY);

        List<?> files = getFiles(e.getTransferable());

        // Inform that the drop is complete.
        e.dropComplete(true);

        files.forEach(o -> {
            File file = (File) o;
            if (file.isFile()) {
                load(file);
            } else {
                File[] listFiles = Objects.requireNonNull(file.listFiles());
                for (File listFile : listFiles)
                    load(listFile);
            }
        });
    }

    private void load(File file) {
        if (FileExtensionUtils.isPropertiesFile(file))
            // If is *.properties file:
            saveLoadManager.load(file, textArea);
        else if (FileExtensionUtils.isAudioFile(file))
            // If is audio file:
            saveLoadManager.setLoadedAudio(file);
    }
}
