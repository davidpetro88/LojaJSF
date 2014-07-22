package com.aula.model.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.aula.model.User;
import com.aula.model.dao.exceptions.NonexistentEntityException;
import com.aula.model.utils.Messages;

public class UserDAO {
	public UserDAO(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}

	public void save(User user) {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			em.persist(user);
			em.getTransaction().commit();
			Messages.addMessage("Salvo", "Usuário " + user.getNome()
					+ " salvo com sucesso!");
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void edit(User user) throws NonexistentEntityException, Exception {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			user = em.merge(user);
			em.getTransaction().commit();
			Messages.addMessage("Alterado!", "Usuário alterado com sucesso!");
		} catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			if (msg == null || msg.length() == 0) {
				Integer id = user.getId();
				if (findUser(id) == null) {
					throw new NonexistentEntityException("The User with id "
							+ id + " no longer exists.");
				}
			}
			throw ex;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public void destroy(Integer id) throws NonexistentEntityException {
		EntityManager em = null;
		try {
			em = getEntityManager();
			em.getTransaction().begin();
			User user;
			try {
				user = em.getReference(User.class, id);
				user.getId();
			} catch (EntityNotFoundException enfe) {
				throw new NonexistentEntityException("The User with id " + id
						+ " no longer exists.", enfe);
			}
			em.remove(user);
			em.getTransaction().commit();
			Messages.addMessage("Excluído!", "Usuário " + user.getNome()
					+ " excluído com sucesso!");
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public List<User> findUserEntities() {
		return findUserEntities(true, -1, -1);
	}

	public List<User> findUserEntities(int maxResults, int firstResult) {
		return findUserEntities(false, maxResults, firstResult);
	}

	private List<User> findUserEntities(boolean all, int maxResults,
			int firstResult) {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			cq.select(cq.from(User.class));
			Query q = em.createQuery(cq);
			if (!all) {
				q.setMaxResults(maxResults);
				q.setFirstResult(firstResult);
			}
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	public User findUser(Integer id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(User.class, id);
		} finally {
			em.close();
		}
	}

	public int getUserCount() {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<User> rt = cq.from(User.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

}
