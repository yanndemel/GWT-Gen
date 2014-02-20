package com.hiperf.common.ui.server.storage;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import com.hiperf.common.ui.client.INakedObject;

public interface IImportValidator<T extends INakedObject> {

	List<INakedObject> validate(T obj, List<INakedObject> ctx, EntityManager em, HttpServletRequest req) throws ImportValidationException;

}
