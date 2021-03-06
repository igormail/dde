package ua.com.integer.dde.res;

import ua.com.integer.dde.res.loading.LoadManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Менеджер ресурсов. Содержит набор {@link LoadManager}. 
 * Вы можете динамически добавить новые менеджеры в этот класс.
 * 
 * @author 1nt3g3r
 */
public class ResourceManager implements Disposable, LoadManager {
	private Array<LoadManager> loadManagers;

	public ResourceManager() {
		loadManagers = new Array<LoadManager>();
	}
	
	/**
	 * Помещает все ресурсы со всех менеджеров загрузки в очередь. По сути, вызывает 
	 * loadAll() у всех обьектов класса LoadManager
	 */
	public void loadAll() {
		for(LoadManager loadManager : loadManagers) {
			loadManager.loadAll();
		}
	}
	
	/**
	 * Ищет первый менеджер загрузки, который еще не отработал (которому есть еще что грузить). 
	 * Если находит - грузит очередную порцию ресурсов из него, возвращает false. Если же все загружено - 
	 * возвращает true.
	 */
	@Override
	public boolean loadStep() {
		for(LoadManager loadManager : loadManagers) {
			if (loadManager.getLoadPercent() < 1) {
				loadManager.loadStep();
				return false;
			}	
		}
		return true;
	}
	
	/**
	 * Возвращает суммарный прогресс загрузки для всех менеджеров.
	 */
	public float getLoadPercent() {
		float totalProgress = 0;
		for(LoadManager loadManager : loadManagers) {
			totalProgress += loadManager.getLoadPercent();
		}
		return totalProgress / (float) loadManagers.size;
	}
	
	/**
	 * Очищает все менеджеры загрузки, вызывая в них метод dispose(). 
	 */
	@Override
	public void dispose() {
		for(LoadManager loadManager : loadManagers) {
			loadManager.dispose();
		}
	}
	
	/**
	 * Возвращает нужный нам LoadManager по его классу. Например, если мы хотим получить экземпляр TextureManager - 
	 * вызываем getManager(TextureManager.class), и он вернет нам экземпляр TextureManager. Если же нет обьекта для 
	 * запрошенного класса (вы не добавили ранее этот менеджер загрузки), будет брошено исключение IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public <T extends LoadManager> T getManager(Class<T> type) {
		for(LoadManager loadManager : loadManagers) {
			if (loadManager.getClass() == type) {
				return (T) loadManager;
			}
		}
		throw new IllegalArgumentException("No load manager for this class!");
	}
	
	/**
	 * Добавляет менеджер загрузки в этот менеджер ресурсов. 
	 * Вы передаете лишь класс менеджера загрузки, в этом методе создается его экземпляр. 
	 * Следует заметить, что в этому случае у вашего менеджера загрузки должен быть конструктор по умолчанию - без параметров. 
	 * Позже вы можете обратиться к добавленному менеджеру загрузки через вызов {@link #getManager(Class)}
	 */
	public <T extends LoadManager> void addManager(Class<T> type) {
		try {
			LoadManager loadManager = type.newInstance();
			loadManagers.add(loadManager);
		} catch (Exception e) {
			Gdx.app.error("ResourceManager", "Error during loading " + type + " manager");
		}
	}
	
	/**
	 * Добавляет созданный и инициализированный менеджер загрузки в данный менеджер загрузки.
	 */
	public void addManager(LoadManager manager) {
		loadManagers.add(manager);
	}
}
