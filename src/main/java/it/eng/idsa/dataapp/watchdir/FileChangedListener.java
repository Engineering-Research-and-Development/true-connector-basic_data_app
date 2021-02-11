package it.eng.idsa.dataapp.watchdir;

import java.io.File;

public interface FileChangedListener {

	void notifyAdd(File artifact);

    void notifyRemove(File artifact);
}
