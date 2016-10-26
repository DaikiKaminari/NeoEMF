/*
 * Copyright (c) 2013-2016 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.map.datastore;

import fr.inria.atlanmod.neoemf.datastore.InvalidDataStoreException;
import fr.inria.atlanmod.neoemf.datastore.PersistenceBackend;
import fr.inria.atlanmod.neoemf.datastore.PersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.datastore.estores.PersistentEStore;
import fr.inria.atlanmod.neoemf.datastore.estores.impl.AutocommitEStoreDecorator;
import fr.inria.atlanmod.neoemf.datastore.impl.AbstractPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.logger.NeoLogger;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.CachedManyDirectWriteMapResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.DirectWriteMapResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.DirectWriteMapWithIndexesResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.datastore.estores.impl.DirectWriteMapWithListsResourceEStoreImpl;
import fr.inria.atlanmod.neoemf.map.resources.MapResourceOptions.EStoreMapOption;
import fr.inria.atlanmod.neoemf.map.util.NeoMapURI;
import fr.inria.atlanmod.neoemf.resources.PersistentResource;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceOptions;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public final class MapPersistenceBackendFactory extends AbstractPersistenceBackendFactory {

	private static PersistenceBackendFactory INSTANCE;

	public static PersistenceBackendFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MapPersistenceBackendFactory();
		}
		return INSTANCE;
	}

    public static final String MAPDB_BACKEND = "mapdb";

	private MapPersistenceBackendFactory() {
	}
    
	@Override
	public PersistenceBackend createTransientBackend() {
	    //Engine mapEngine = DBMaker.newMemoryDB().makeEngine();
		DB db = DBMaker.memoryDB().make();
		return new MapPersistenceBackend(db);
	}

	@Override
	public PersistentEStore createTransientEStore(PersistentResource resource, PersistenceBackend backend) {
		checkArgument(backend instanceof MapPersistenceBackend,
				"Trying to create a Map-based EStore with an invalid backend: " + backend.getClass().getName());

		return new DirectWriteMapResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
	}

	@Override
	public PersistenceBackend createPersistentBackend(File file, Map<?, ?> options) throws InvalidDataStoreException {
	    File dbFile = FileUtils.getFile(NeoMapURI.createNeoMapURI(URI.createFileURI(file.getAbsolutePath()).appendSegment("neoemf.mapdb")).toFileString());
	    if (!dbFile.getParentFile().exists()) {
			try {
				Files.createDirectories(dbFile.getParentFile().toPath());
			} catch (IOException e) {
				NeoLogger.error(e);
			}
		}
	    PropertiesConfiguration neoConfig;
	    Path neoConfigPath = Paths.get(file.getAbsolutePath()).resolve(NEO_CONFIG_FILE);
        try {
            neoConfig= new PropertiesConfiguration(neoConfigPath.toFile());
        } catch (ConfigurationException e) {
            throw new InvalidDataStoreException(e);
        }
        if (!neoConfig.containsKey(BACKEND_PROPERTY)) {
            neoConfig.setProperty(BACKEND_PROPERTY, MAPDB_BACKEND);
        }
		try {
            neoConfig.save();
        } catch(ConfigurationException e) {
            NeoLogger.error(e);
        }
	    //Engine mapEngine = DBMaker.newFileDB(dbFile).cacheLRUEnable().mmapFileEnableIfSupported().asyncWriteEnable().makeEngine();
	    /*
         * TODO Check the difference when asyncWriteEnable() is set.
         * It has been desactived for MONDO deliverable but not well tested
         */


	    //Engine mapEngine = DBMaker.newFileDB(dbFile).cacheLRUEnable().mmapFileEnableIfSupported().makeEngine();
		DB db = DBMaker.fileDB(dbFile).fileMmapEnableIfSupported().make();
        return new MapPersistenceBackend(db);
	}

	@Override
	protected PersistentEStore internalCreatePersistentEStore(PersistentResource resource, PersistenceBackend backend, Map<?,?> options) throws InvalidDataStoreException {
		checkArgument(backend instanceof MapPersistenceBackend,
				"Trying to create a Map-based EStore with an invalid backend: "+backend.getClass().getName());

		PersistentEStore eStore = null;
		@SuppressWarnings("unchecked")
		List<PersistentResourceOptions.StoreOption> storeOptions = (List<PersistentResourceOptions.StoreOption>)options.get(PersistentResourceOptions.STORE_OPTIONS);
		// Store
		if(storeOptions == null || storeOptions.isEmpty() || storeOptions.contains(EStoreMapOption.DIRECT_WRITE) || (storeOptions.size() == 1 && storeOptions.contains(EStoreMapOption.AUTOCOMMIT))) {
			eStore = new DirectWriteMapResourceEStoreImpl(resource, (MapPersistenceBackend)backend);
        }
        else if (storeOptions.contains(EStoreMapOption.CACHED_MANY)) {
			eStore = new CachedManyDirectWriteMapResourceEStoreImpl(resource, (MapPersistenceBackend) backend);
        }
        else if (storeOptions.contains(EStoreMapOption.DIRECT_WRITE_WITH_LISTS)) {
			eStore = new DirectWriteMapWithListsResourceEStoreImpl(resource, (MapPersistenceBackend) backend);
        }
        else if (storeOptions.contains(EStoreMapOption.DIRECT_WRITE_WITH_INDEXES)) {
			eStore = new DirectWriteMapWithIndexesResourceEStoreImpl(resource, (MapPersistenceBackend) backend);
        }
        // Autocommit
        if (eStore != null) {
			if(storeOptions != null && storeOptions.contains(EStoreMapOption.AUTOCOMMIT)) {
				eStore = new AutocommitEStoreDecorator(eStore);
            }
        } else {
            throw new InvalidDataStoreException();
        }
		return eStore;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void copyBackend(PersistenceBackend from, PersistenceBackend to) {
		checkArgument(from instanceof MapPersistenceBackend,
				"The backend to copy is not an instance of MapPersistenceBackend");
		checkArgument(to instanceof MapPersistenceBackend,
				"The target copy backend is not an instance of MapPersistenceBackend");

	    MapPersistenceBackend source = (MapPersistenceBackend)from;
	    MapPersistenceBackend target = (MapPersistenceBackend)to;

		source.copyTo(target);

	}
}
