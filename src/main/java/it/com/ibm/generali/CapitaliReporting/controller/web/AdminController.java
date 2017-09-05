package it.com.ibm.generali.CapitaliReporting.controller.web;

import it.com.ibm.generali.CapitaliReporting.CapitaliReportingApplication;
import it.com.ibm.generali.CapitaliReporting.dao.RoleDao;
import it.com.ibm.generali.CapitaliReporting.dao.UserDao;
import it.com.ibm.generali.CapitaliReporting.model.Role;
import it.com.ibm.generali.CapitaliReporting.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController extends SessionHelper
{
    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    private UserDao users;
    private RoleDao roles;

    @Autowired
    public AdminController(UserDao userDao, RoleDao roleDao)
    {
        this.users = userDao;
        this.roles = roleDao;
    }

    /**
     * Configure GET with selected user
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"selecteduser"})
    public String configureWithUsername(Model model, HttpSession session, @RequestParam("selecteduser") String username)
    {
        logger.info("/configure page with selecteduser =" + username);
        return this.configureTemplate(model, session, username);
    }

    /**
     * Configure GET
     *
     * @param model the HTTP request attributes. it will updated
     *              with application's version.
     * @return the configure page
     */
    @RequestMapping("/configure")
    public String configure(Model model, HttpSession session)
    {
        logger.info("/configure GET");
        return this.configureTemplate(model, session, "none");
    }

    /**
     * Configure GET with mode
     *
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"mode"})
    public String configureWithMode(Model model, @RequestParam("mode") String mode, HttpSession session)
    {
        logger.info("/configure GET with mode=" + mode);
        return this.configureTemplate(model, session, mode);
    }

    /**
     * Configure with delete
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"delete"})
    public String deleteUser(Model model, HttpSession session, @RequestParam("delete") String username)
    {
        this.users.delete(username);
        return "redirect:configure?mode=ok_deleted";
    }

    /**
     * Configure POST
     */
    @RequestMapping(value = "/configure", method = RequestMethod.POST)
    public String addUser(@ModelAttribute("user") User user)
    {
        logger.info("/configure POST");
        String username = user.getUsername();
        String mode;

        User modUser = this.users.findOne(username);
        if (modUser != null)
        {
            modUser.setEmail(user.getEmail());
            modUser.setFullName(user.getFullName());
            modUser.setActive(user.getActive());
            mode = "ok_modified";
        }
        else
        {
            modUser = User.Factory.copy(user);
            mode = "ok_added";
        }

        this.users.save(modUser);
        return "redirect:configure?mode=" + mode;
    }

    /**
     * Roles with delete
     */
    @RequestMapping(value = "/roles", method = RequestMethod.GET, params = {"delete"})
    public String deleteRole(@RequestParam("delete") String roleId, HttpSession session)
    {
        if (!this.isAdmin(session))
        {
            return "redirect:login";
        }

        this.roles.delete(Long.parseLong(roleId));
        return "redirect:roles";
    }

    /**
     * Roles GET
     */
    @RequestMapping("/roles")
    public String roles(Model model, HttpSession session)
    {
        logger.info("/roles page");

        if (!this.isAdmin(session))
        {
            return "redirect:login";
        }

        final Iterable<Role> roles = this.roles.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("user", this.getCurrentUser(session));
        model.addAttribute("title", CapitaliReportingApplication.APP_TITLE);
        model.addAttribute("version", CapitaliReportingApplication.getVersion());

        return "roles";
    }

    /**
     * Roles POST
     */
    @RequestMapping(value = "/roles", method = RequestMethod.POST)
    public String addRole(@RequestParam("description") String description, HttpSession session)
    {
        if (!this.isAdmin(session))
        {
            return "redirect:login";
        }

        logger.info("Received POST for description = " + description);

        Role newrole = new Role();
        newrole.setDescription(description);

        this.roles.save(newrole);

        return "redirect:roles";

    }

    private String configureTemplate(Model model, HttpSession session, String mode)
    {
        if (!this.isAdmin(session))
        {
            return "redirect:login";
        }

        User selectedUser;
        final Iterable<User> users = this.users.findAll();
        final Iterable<Role> roles = this.roles.findAll();

        List<User> allUsersExceptAdmin = new ArrayList<>();
        users.forEach(allUsersExceptAdmin::add);
        allUsersExceptAdmin = allUsersExceptAdmin.stream().filter(user -> !user.username.equals("admin")).collect(Collectors.toList());

        if (mode.equals("new") || (mode.startsWith("ok")))
        {
            selectedUser = new User();
            selectedUser.username = "";
            selectedUser.password = "";
            selectedUser.email = "";
            selectedUser.fullName = "";
            selectedUser.setActive(false);
        }
        else
        {
            if (mode.equals("none"))
            {
                selectedUser = allUsersExceptAdmin.iterator().next();
            }
            else
            {
                selectedUser = this.users.findOne(mode);
            }
        }

        model.addAttribute("users", allUsersExceptAdmin);
        model.addAttribute("mode", mode);
        model.addAttribute("roles", roles);
        model.addAttribute("user", this.getCurrentUser(session));
        model.addAttribute("selecteduser", selectedUser);
        model.addAttribute("title", CapitaliReportingApplication.APP_TITLE);
        model.addAttribute("version", CapitaliReportingApplication.getVersion());

        return "configure";

    }


}
