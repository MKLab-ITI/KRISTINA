package gr.iti.kristina.repository;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;

public class GraphDbRepositoryManager {

	private RemoteRepositoryManager _manager;

	public GraphDbRepositoryManager(String serverURL, String username, String password) {

		try {
			_manager = new RemoteRepositoryManager(serverURL);
			_manager.setUsernameAndPassword(username, password);
			_manager.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public GraphDbRepositoryManager(String serverURL) {

		try {
			_manager = new RemoteRepositoryManager(serverURL);
			_manager.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Repository getRepository(String id) throws RepositoryConfigException, RepositoryException {
		return _manager.getRepository(id);
	}

	public void shutDown(String CONTEXT) {
		System.out.println("closing GraphDb manager [" + CONTEXT + "]");
		if (_manager != null) {
			try {
				_manager.shutDown();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public RemoteRepositoryManager getManager() {
		return _manager;
	}

}
