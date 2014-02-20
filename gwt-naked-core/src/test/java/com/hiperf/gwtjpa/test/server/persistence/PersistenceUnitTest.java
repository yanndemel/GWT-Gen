package com.hiperf.gwtjpa.test.server.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.agoncal.application.petstore.domain.Address;
import org.agoncal.application.petstore.domain.Category;
import org.agoncal.application.petstore.domain.CreditCard;
import org.agoncal.application.petstore.domain.CreditCardType;
import org.agoncal.application.petstore.domain.Customer;
import org.agoncal.application.petstore.domain.Hobby;
import org.agoncal.application.petstore.domain.Item;
import org.agoncal.application.petstore.domain.Order;
import org.agoncal.application.petstore.domain.OrderLine;
import org.agoncal.application.petstore.domain.Product;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.ObjectsToPersist;
import com.hiperf.common.ui.client.exception.PersistenceException;
import com.hiperf.common.ui.server.listener.GlobalParams;
import com.hiperf.common.ui.server.storage.IPersistenceHelper;
import com.hiperf.common.ui.server.storage.IStorageService;
import com.hiperf.common.ui.server.storage.impl.PersistenceHelper;
import com.hiperf.common.ui.server.storage.impl.StorageService;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.util.Id;
import com.hiperf.common.ui.shared.util.NakedObjectsList;

public class PersistenceUnitTest {

    private static Logger logger = Logger.getLogger(PersistenceUnitTest.class.getName());
	private int count = 0;


    public PersistenceUnitTest() {
        super();
    }

    @BeforeClass
    public static void initDB() {
        try {
            logger.info("Starting in-memory HSQL database for unit tests");
            GlobalParams p = GlobalParams.getInstance();
			p.setTransactionType(IPersistenceHelper.TYPE.LOCAL.name());
			p.setUnitName("petstore");
            StorageService.getInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception during HSQL database startup.");
        }
    }

    @AfterClass
    public static void shutDown() {
        logger.info("Shuting down Hibernate JPA layer.");
        StorageService.getInstance().shutdown();
    }

    @Test
    public void testInsert() {
    	ArrayList<INakedObject> insertedObjects = new ArrayList<INakedObject>();
    	Address a = newAddress();
		insertedObjects.add(a);
    	Long id = a.getId();
		Map<Id, INakedObject> res = null;
    	
		try {
			res = StorageService.getInstance().persist(new ObjectsToPersist(insertedObjects, null, null, null, null), "toto", Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		assertTrue(res != null);
		List<String> idF = new ArrayList<String>();
		idF.add("id");		
		List<Object> idO = new ArrayList<Object>();
		idO.add(id);
		Id id2 = new Id(idF , idO );
		Id idLoc = id2;
		INakedObject no = res.get(idLoc);
		assertTrue(no != null);
		assertTrue(no instanceof Address);
		Address ad = (Address) no;
		assertTrue(ad.getId() >= 0);
		idO.clear();
		idO.add(ad.getId());
		String s = null;
		try {
			no = StorageService.getInstance().get(Address.class.getName(), id2);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		assertTrue(no != null);
		assertTrue(no instanceof Address);
		
		try {
			s = StorageService.getInstance().checkExists(Address.class.getName(), "id", id2.getFieldValues().get(0).toString(), Locale.FRENCH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		assertTrue(s != null);
		
		
		Map<String, Set<Id>> removedObjectsIdsByClassName = new HashMap<String, Set<Id>>();
		HashSet<Id> value = new HashSet<Id>();
		value.add(id2);
		removedObjectsIdsByClassName.put(Address.class.getName(), value);
		try {
			StorageService.getInstance().persist(new ObjectsToPersist(null, null, removedObjectsIdsByClassName , null, null), "toto", Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		
		try {
			s = StorageService.getInstance().checkExists(Address.class.getName(), "id", id2.getFieldValues().get(0).toString(), Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		assertTrue(s == null);
		
    }

	private Address newAddress() {
		Address a = new Address();
		a.setId(PersistenceManager.nextLongId());
    	a.setStreet1((++count )+", rue de la Paix");
    	a.setCity("Paris");
    	a.setCountry("FRANCE");
    	a.setState("IDF");
    	a.setZipcode("75008");
		return a;
	}
    
    @Test
    public void testBigTransaction() {
    	ArrayList<INakedObject> insertedObjects = new ArrayList<INakedObject>();
    	Address a1 = newAddress();
    	insertedObjects.add(a1);
		Customer c = new Customer("John", "Smith", "john", "password", "john@gg.com", a1);
    	Long cId = PersistenceManager.nextLongId();
		c.setId(cId);
    	insertedObjects.add(c);
    	Hobby h1 = newHobby("Foot");
    	Hobby h2 = newHobby("Basket");
    	insertedObjects.add(h1);
    	insertedObjects.add(h2);
    	Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName = new HashMap<String, Map<Id,Map<String,List<Id>>>>();
    	HashMap<Id, Map<String, List<Id>>> map = new HashMap<Id, Map<String, List<Id>>>();
    	HashMap<String, List<Id>> map2 = new HashMap<String, List<Id>>();
    	List<Id> l = new ArrayList<Id>();
    	try {
			l.add(PersistenceHelper.getInstance().getId(h1));
			l.add(PersistenceHelper.getInstance().getId(h2));
		} catch (IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
    	
    	map2.put("hobbies", l);
		try {
			map.put(PersistenceHelper.getInstance().getId(c), map2);
		} catch (IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
		manyToManyAddedByClassName.put(Customer.class.getName(), map);
		Address a2 = newAddress();
    	insertedObjects.add(a2);
    	Map<Id, INakedObject> res = null;
    	IStorageService ss = StorageService.getInstance();
		try {
			res = ss.persist(new ObjectsToPersist(insertedObjects, null, null, manyToManyAddedByClassName, null), "toto", Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		assertTrue(res != null);
		INakedObject no = null;
		
		Id oldCusId = newId(cId);
		no = res.get(oldCusId);
		assertTrue(no != null && no instanceof Customer);
    	List<INakedObject> all = null;
    	try {
			all = ss.getAll(Hobby.class.getName(), "select o from Hobby o");
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
    	assertEquals(2, all.size());
    	for(INakedObject oo : all) {
    		Hobby hh = (Hobby) oo;
    		if(hh.getName().equals(h2.getName())) {
    			h2 = hh;
    			break;
    		}
    	}
    	ObjectsToPersist toPersist = getObjectsToPersist(c, h2);
    	testCollection(ss, no, toPersist);
    	toPersist = getObjectsToPersist(c, h2);
    	try {
			res = ss.persist(toPersist, "toto", Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
    	testCollection(ss, no, new ObjectsToPersist());
    	
    	
    	CreditCard cc = new CreditCard("002154687", CreditCardType.VISA, "0115");
    	cc.setId(PersistenceManager.nextLongId());
		Order o = new Order(c, cc,  c.getHomeAddress());
    	Long oId = PersistenceManager.nextLongId();
		o.setId(oId);
    	insertedObjects.clear();
    	insertedObjects.add(cc);
    	insertedObjects.add(o);
    	Category cat = new Category("Book", "IT Book");
    	cat.setId(PersistenceManager.nextLongId());
		Product prd = new Product("Book", "IT book", cat);
		prd.setId(PersistenceManager.nextLongId());
		Item it = new Item("GWT Manual", 12.25f, null, prd, "GWT advanced");
		it.setId(PersistenceManager.nextLongId());
		insertedObjects.add(cat);
		insertedObjects.add(prd);
		insertedObjects.add(it);
		OrderLine ol = new OrderLine(24, it);
		
		Long olId = PersistenceManager.nextLongId();
		ol.setId(olId);
		insertedObjects.add(ol);
		
		
		Map<String, Map<Id, Map<String, Serializable>>> updatedObjects = new HashMap<String, Map<Id,Map<String,Serializable>>>();
		HashMap<Id, Map<String, Serializable>> map3 = new HashMap<Id, Map<String, Serializable>>();
		HashMap<String, Serializable> map4 = new HashMap<String, Serializable>();
		map4.put("email", "juju@blabla.fr");
		map3.put(newId(((Customer)no).getId()), map4);
		updatedObjects.put(Customer.class.getName(), map3);
		ObjectsToPersist tp = new ObjectsToPersist(insertedObjects, updatedObjects , null, null, null);
		try {
			res = ss.persist(tp, "toto", Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		assertTrue(res.size() > 0);
		INakedObject noo = res.get(newId(olId));
		assertTrue(noo != null && noo instanceof OrderLine);;
		OrderLine line = (OrderLine) noo;
		try {
			INakedObject ino = ss.get(OrderLine.class.getName(), newId(line.getId()));
			assertTrue(ino != null);
			assertTrue(ino.equals(noo));
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		INakedObject iNakedObject = res.get(newId(oId));
		assertTrue(iNakedObject != null && iNakedObject instanceof Order);
		Order order = (Order) iNakedObject;
		
		updatedObjects = new HashMap<String, Map<Id,Map<String,Serializable>>>();
		map3 = new HashMap<Id, Map<String, Serializable>>();
		map4 = new HashMap<String, Serializable>();
		map4.put("order", order);
		map3.put(newId(line.getId()), map4);
		updatedObjects.put(OrderLine.class.getName(), map3);
		tp = new ObjectsToPersist(null, updatedObjects , null, null, null);
		try {
			res = ss.persist(tp, "toto", Locale.ENGLISH);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		
		
		try {
			Collection<INakedObject> collection = ss.getCollection(Order.class.getName(), newId(order.getId()), "orderLines");
			assertTrue(collection != null && collection.size() > 0);
			collection = ss.getAll(OrderLine.class.getName(), "select o from OrderLine o");
			assertTrue(collection != null && collection.size() > 0);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		try {
			Map<String, String> all2 = ss.getAll(Order.class.getName(), "o.customer.lastname = '"+c.getLastname()+"'", "creditCard", CreditCard.class.getName(), "creditCardNumber");
			assertTrue(all2 != null);
			assertTrue(all2.keySet().iterator().next().equals(cc.getCreditCardNumber()));
			NakedObjectsList loadAll = ss.loadAll(Order.class.getName(), "o.customer.lastname = '"+c.getLastname()+"'", 1, 20, "customer.lastname", false, false, new ObjectsToPersist(), Locale.ENGLISH);
			assertTrue(loadAll.getCount() == 1);
			Collection<INakedObject> collection = ss.getCollection(Order.class.getName(), 
					newId(((Order)loadAll.getList().get(0)).getId()), "orderLines");
			assertTrue(collection != null && collection.size() == 1);
			NakedObjectsList sColl = ss.getSortedCollection(OrderLine.class.getName(), 
					newId(((Order)loadAll.getList().get(0)).getId()), "order", "quantity", false, 
					1, 20, new ObjectsToPersist());
			assertTrue(sColl != null && sColl.getList().size() == 1);
		} catch (PersistenceException e) {
			fail(e.getMessage());
		}
		
    }

	private void testCollection(IStorageService ss, INakedObject no,
			ObjectsToPersist toPersist) {
		try {
			Id cusId = PersistenceHelper.getInstance().getId(no);
			NakedObjectsList coll = ss.getCollectionInverse(Customer.class.getName(), "hobbies", cusId, 1, 20, toPersist);
			assertTrue(coll.getCount() == 2);
			List ll = Arrays.asList(new String[]{"Tennis", "Foot"});
			INakedObject o = coll.getList().get(0);
			assertTrue(o instanceof Hobby);
			Hobby h = (Hobby) o;
			assertTrue(ll.contains(h.getName()));
			o = coll.getList().get(1);
			assertTrue(o instanceof Hobby);
			h = (Hobby) o;
			assertTrue(ll.contains(h.getName()));
		} catch (PersistenceException | IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
	}

	private ObjectsToPersist getObjectsToPersist(Customer c, Hobby h2) {
		ArrayList<INakedObject> insertedObjects;
		Hobby h1;
		Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAddedByClassName;
		HashMap<Id, Map<String, List<Id>>> map;
		HashMap<String, List<Id>> map2;
		List<Id> l;
		insertedObjects = new ArrayList<INakedObject>();
    	
    	h1 = newHobby("Tennis");
    	insertedObjects.add(h1);
    	
    	manyToManyAddedByClassName = new HashMap<String, Map<Id,Map<String,List<Id>>>>();
    	map = new HashMap<Id, Map<String, List<Id>>>();
    	map2 = new HashMap<String, List<Id>>();
    	l = new ArrayList<Id>();
    	try {
			l.add(PersistenceHelper.getInstance().getId(h1));
		} catch (IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
    	map2.put("hobbies", l);
		try {
			map.put(PersistenceHelper.getInstance().getId(c), map2);
		} catch (IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
		manyToManyAddedByClassName.put(Customer.class.getName(), map);
    	
		Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemovedByClassName= new HashMap<String, Map<Id,Map<String,List<Id>>>>();
		map = new HashMap<Id, Map<String, List<Id>>>();
    	map2 = new HashMap<String, List<Id>>();
    	l = new ArrayList<Id>();
    	try {
			l.add(PersistenceHelper.getInstance().getId(h2));
		} catch (IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
    	map2.put("hobbies", l);
		try {
			map.put(PersistenceHelper.getInstance().getId(c), map2);
		} catch (IllegalAccessException | InvocationTargetException e1) {
			fail(e1.getMessage());
		}
		manyToManyRemovedByClassName.put(Customer.class.getName(), map);
		
		ObjectsToPersist toPersist = new ObjectsToPersist(insertedObjects, null, null, manyToManyAddedByClassName, manyToManyRemovedByClassName);
		return toPersist;
	}

	private Id newId(Long id) {
		List<Object> idList = new ArrayList<Object>();
		List<String> idFields = new ArrayList<String>();
		Id oldCusId = new Id(idFields, idList);
		idFields.add("id");
		idList.add(id);
		return oldCusId;
	}

	private Hobby newHobby(String hobby) {
		Long h1Id = PersistenceManager.nextLongId();
    	Hobby h1 = new Hobby();
    	h1.setName(hobby);
    	h1.setId(h1Id);
		return h1;
	}

}
