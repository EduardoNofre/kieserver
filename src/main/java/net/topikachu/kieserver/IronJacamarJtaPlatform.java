package net.topikachu.kieserver;

import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

public class IronJacamarJtaPlatform extends AbstractJtaPlatform {
    public static final String TM_NAME = "java:/TransactionManager";
    public static final String UT_NAME = "java:/UserTransaction";

    @Override
    protected TransactionManager locateTransactionManager() {
        return (TransactionManager) jndiService().locate( TM_NAME );
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return (UserTransaction) jndiService().locate( UT_NAME );
    }
}
