package com.simpledi;

import com.simpledi.data.dao.event.EventDAO;
import com.simpledi.data.dao.event.InMemoryEventDAO;
import com.simpledi.data.dao.profile.InMemoryProfileDAO;
import com.simpledi.data.dao.profile.ProfileDAO;
import com.simpledi.data.dao.test.InMemoryTestDao;
import com.simpledi.data.dao.test.TestDao;
import com.simpledi.data.service.DefaultConstructorFindPreMaxService;
import com.simpledi.data.service.EventService;
import com.simpledi.data.service.InjectAmbiguityService;
import com.simpledi.data.service.NoSuitableConstructorService;
import com.simpledi.data.thread.TestRunnable;
import com.simpledi.exception.BindingNotFoundException;
import com.simpledi.exception.ConstructorAmbiguityException;
import com.simpledi.exception.NoSuitableConstructorException;
import com.simpledi.ioc.Injector;
import com.simpledi.ioc.InjectorImpl;
import com.simpledi.ioc.Provider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {

    @Test
    void testExistingBinding() {
        Injector injector = new InjectorImpl();
        injector.bind(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        assertNotNull(daoProvider);
        EventDAO firstInstance = daoProvider.getInstance();
        assertNotNull(firstInstance);

        EventDAO secondInstance = daoProvider.getInstance();
        assertSame(InMemoryEventDAO.class, secondInstance.getClass());
        assertNotSame(firstInstance, secondInstance);
    }

    @Test
    void testNonExistingBinding() {
        Injector injector = new InjectorImpl();
        assertNull(injector.getProvider(EventDAO.class));
    }

    @Test
    void testUniqBinding() {
        Injector injector = new InjectorImpl();
        injector.bind(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        assertNotSame(daoProvider.getInstance(), daoProvider.getInstance());
    }

    @Test
    void testSingletonBinding() {
        Injector injector = new InjectorImpl();
        injector.bindSingleton(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        assertSame(daoProvider.getInstance(), daoProvider.getInstance());
    }

    @Test
    void testSingletonBindingFewThreads() throws InterruptedException {
        Injector injector = new InjectorImpl();
        injector.bindSingleton(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        ExecutorService service = Executors.newFixedThreadPool(3);
        List<Future<EventDAO>> results = service.invokeAll(Arrays.asList(new TestRunnable(daoProvider),
                new TestRunnable(daoProvider), new TestRunnable(daoProvider)));

        try {
            EventDAO resInstance = results.isEmpty() ? null : results.get(0).get();
            for (Future<EventDAO> result : results) {
                assertSame(resInstance, result.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        service.shutdown();

        EventDAO instance = daoProvider.getInstance();
        System.out.println("main() thread instance1: " + instance);

        EventDAO instance2 = daoProvider.getInstance();
        System.out.println("main() thread instance2: " + instance2);

        assertSame(instance, instance2);
    }

    @Test
    void testBindingFewThreads() throws InterruptedException {
        Injector injector = new InjectorImpl();
        injector.bind(EventDAO.class, InMemoryEventDAO.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);

        ExecutorService service = Executors.newFixedThreadPool(3);
        List<Future<EventDAO>> results = service.invokeAll(Arrays.asList(new TestRunnable(daoProvider),
                new TestRunnable(daoProvider), new TestRunnable(daoProvider)));

        try {
            EventDAO resInstance = null;
            for (Future<EventDAO> result : results) {
                if (resInstance == null) {
                    resInstance = result.get();
                } else {
                    assertNotSame(resInstance, result.get());
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        service.shutdown();
    }

    @Test
    void testDefaultConstructorInjection() {
        Injector injector = new InjectorImpl();
        injector.bind(DefaultConstructorFindPreMaxService.class, DefaultConstructorFindPreMaxService.class);

        Provider<DefaultConstructorFindPreMaxService> serviceProvider = injector.getProvider(DefaultConstructorFindPreMaxService.class);
        assertNotNull(serviceProvider);

        DefaultConstructorFindPreMaxService service = serviceProvider.getInstance();
        assertNotNull(service);

        service.execute();
    }

    @Test
    void testInjection() {
        Injector injector = new InjectorImpl();
        injector.bindSingleton(TestDao.class, InMemoryTestDao.class);
        injector.bindSingleton(EventDAO.class, InMemoryEventDAO.class);
        injector.bindSingleton(ProfileDAO.class, InMemoryProfileDAO.class);
        injector.bindSingleton(EventService.class, EventService.class);

        Provider<EventDAO> daoProvider = injector.getProvider(EventDAO.class);
        Provider<EventService> serviceProvider = injector.getProvider(EventService.class);

        EventService service = serviceProvider.getInstance();

        EventDAO expectedDao = daoProvider.getInstance();
        EventDAO injectedDao;

        try {
            Field daoField = EventService.class.getDeclaredField("dao");
            daoField.setAccessible(true);
            injectedDao = (EventDAO) daoField.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertSame(expectedDao, injectedDao);
    }

    @Test
    void testConstructorAmbiguityException() {
        assertThrows(ConstructorAmbiguityException.class, () -> {
            Injector injector = new InjectorImpl();
            injector.bind(EventDAO.class, InMemoryEventDAO.class);
            injector.bind(ProfileDAO.class, InMemoryProfileDAO.class);
            injector.bind(InjectAmbiguityService.class, InjectAmbiguityService.class);

            Provider<InjectAmbiguityService> serviceProvider = injector.getProvider(InjectAmbiguityService.class);

            // In case of correct implementation the following statement is unreachable
            assertNotNull(serviceProvider);
        });
    }

    @Test
    void testNoSuitableConstructorException() {
        assertThrows(NoSuitableConstructorException.class, () -> {
            Injector injector = new InjectorImpl();
            injector.bind(NoSuitableConstructorService.class, NoSuitableConstructorService.class);

            Provider<NoSuitableConstructorService> serviceProvider = injector.getProvider(NoSuitableConstructorService.class);

            // In case of a correct implementation the following statement is unreachable
            assertNotNull(serviceProvider);
        });
    }

    @Test
    void testBindingNotFoundException() {
        assertThrows(BindingNotFoundException.class, () -> {
            Injector injector = new InjectorImpl();
            injector.bind(EventService.class, EventService.class);

            Provider<EventService> serviceProvider = injector.getProvider(EventService.class);

            // In case of a correct implementation the following statement is unreachable
            assertNotNull(serviceProvider);
        });
    }
}
