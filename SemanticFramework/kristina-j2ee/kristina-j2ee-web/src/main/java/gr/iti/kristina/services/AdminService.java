package gr.iti.kristina.services;

import java.io.IOException;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.rio.RDFParseException;

import gr.iti.kristina.admin.AdminBean;

@RequestScoped
@Path("admin")
public class AdminService {

	@EJB
	AdminBean adminBean;

	@POST
	@Path("/clear-kb")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public boolean clearKb(@FormParam("code") String code) {
		AdminBean adminBean = new AdminBean();
		if (code.equals("samiam#2")) {
			try {
				return adminBean.clearKb();
			} catch (RepositoryException | RepositoryConfigException | RDFParseException | IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;

	}

	public static void main(String[] args) {
		AdminService admin = new AdminService();
		System.out.println(admin.clearKb("samiam#2"));
	}

}
