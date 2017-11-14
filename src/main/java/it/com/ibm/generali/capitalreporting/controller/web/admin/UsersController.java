package it.com.ibm.generali.capitalreporting.controller.web.admin;

import it.com.ibm.generali.capitalreporting.CapitalReportingApplication;
import it.com.ibm.generali.capitalreporting.controller.web.SessionHelper;
import it.com.ibm.generali.capitalreporting.dao.RoleDao;
import it.com.ibm.generali.capitalreporting.dao.UserDao;
import it.com.ibm.generali.capitalreporting.dao.UserTagDao;
import it.com.ibm.generali.capitalreporting.framework.Operation;
import it.com.ibm.generali.capitalreporting.framework.Utilities;
import it.com.ibm.generali.capitalreporting.model.CapitalUser;
import it.com.ibm.generali.capitalreporting.model.Role;
import it.com.ibm.generali.capitalreporting.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class UsersController extends SessionHelper
{
    private Logger logger = LoggerFactory.getLogger(UsersController.class);

    private UserDao users;
    private RoleDao roles;
    private UserTagDao tags;
    private UserService userService;

    @Autowired
    public UsersController(UserDao userDao, RoleDao roleDao, UserTagDao tagDao, UserService userService)
    {
        this.users = userDao;
        this.roles = roleDao;
        this.tags = tagDao;
        this.userService = userService;
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
        CapitalUser selectedUser = Utilities.INSTANCE.getFirstNonAdminUser(this.users);
        return "redirect:configure?selecteduser=" + selectedUser.getUsername();
    }

    /**
     * Configure GET with selected User
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"selecteduser"})
    public String configureWithUsername(Model model,
                                        HttpSession session,
                                        @RequestParam("selecteduser") String username)
    {
        logger.info("/configure page with selecteduser =" + username);
        model.addAttribute("tags", this.tags.findAll());
        return this.configureTemplate(model, session, username);
    }

    /**
     * Configure GET with Mode
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"mode"})
    public String configureWithMode(Model model,
                                    HttpSession session,
                                    @RequestParam("mode") String mode)
    {
        CapitalUser selectedUser = Utilities.INSTANCE.getFirstNonAdminUser(this.users);
        String username = selectedUser.getUsername();
        logger.info("/configure page with mode =" + mode);
        model.addAttribute("tags", this.tags.findAll());
        return this.configureTemplate(model, session, this.getOperation(mode), username);
    }

    /**
     * Configure GET with mode and selecteduser
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"mode", "selecteduser"})
    public String configureWithMode(Model model,
                                    HttpSession session,
                                    @RequestParam("mode") String mode,
                                    @RequestParam("selecteduser") String username)
    {
        logger.info("/configure GET with mode=" + mode);
        model.addAttribute("tags", this.tags.findAll());
        return this.configureTemplate(model, session, this.getOperation(mode), username);
    }

    /**
     * Configure GET with delete
     */
    @RequestMapping(value = "/configure", method = RequestMethod.GET, params = {"delete"})
    public String deleteUser(Model model, HttpSession session, @RequestParam("delete") String username)
    {
        this.users.delete(username);
        return "redirect:configure?mode=ok_deleted";
    }

    /**
     * Configure POST
     * Add or modify User
     */
    @RequestMapping(value = "/configure", method = RequestMethod.POST)
    public String addOrModifyUser(@ModelAttribute("user") CapitalUser user,
                                  @RequestParam(value = "active", defaultValue = "false") boolean isActive)
    {
        logger.info("/configure POST");
        String username = user.getUsername();
        String mode;
        CapitalUser modUser = this.users.findOne(username);

        logger.info("User is active? > " + isActive);

        if (modUser != null)
        {
            modUser.setEmail(user.getEmail());
            modUser.setFullName(user.getFullName());
            modUser.setActive(isActive);
            modUser.setRoles(user.getRoles());
            modUser.setUsertags(user.getUsertags());
            mode = "ok_modified";
        }
        else
        {
            modUser = CapitalUser.Factory.copy(user);
            mode = "ok_added";
        }

        this.users.save(modUser);
        return "redirect:configure?mode=" + mode + "&selecteduser=" + modUser.getUsername();
    }

    /**
     * Roles with copy
     */
    @RequestMapping(value = "/copyuser", method = RequestMethod.GET, params = {"username", "username_new"})
    public String copyScope(Model model, HttpSession session, @RequestParam("username") String username, @RequestParam("username_new") String usernameNew)
    {
        String redirect = "redirect:/configure";
        try
        {
            userService.copyUser(username, usernameNew);
            redirect = "redirect:/configure?selecteduser=" + usernameNew;
        }
        catch (Exception exc)
        {
            logger.error(exc.getMessage());
        }

        return redirect;
    }

    @RequestMapping(value = "/uploadUsersFile", method = RequestMethod.POST)
    public String uploadUsersFile(@RequestParam("file") MultipartFile file)
    {
        BufferedReader br = null;
        String line;
        List<CapitalUser> utenti = new ArrayList<>();
        try
        {

            br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while ((line = br.readLine()) != null)
            {
                String[] data = line.split(";");
                CapitalUser user = this.users.findOne(data[0]);
                if (user != null)
                {
                    user.setPassword(data[1]);
                    user.setFullName(data[2]);
                    user.setEmail(data[3]);
                    Set<Role> roles = new HashSet<>();
                    roles.add(this.roles.findByDescription(data[4]));
                    user.setRoles(roles);
                    utenti.add(user);
                }
                else
                {
                    Role role = this.roles.findByDescription(data[4]);
                    if (role != null)
                    {
                        utenti.add(new CapitalUser(data[0], data[1], data[2], data[3], role));
                    }
                    else
                    {
                        logger.warn("Cannot add user: role not found.");
                    }
                }
            }
            this.users.save(utenti);
            List<CapitalUser> all = (List<CapitalUser>) this.users.findAll();
            System.out.println(all);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return "redirect:configure";
    }


    private String configureTemplate(Model model,
                                     HttpSession session,
                                     String username)
    {
        return this.configureTemplate(model, session, Operation.NONE, username);
    }

    private String configureTemplate(Model model,
                                     HttpSession session,
                                     Operation operation,
                                     String username)
    {
        if (!this.isAdmin(session))
        {
            return "redirect:login";
        }

        final Iterable<Role> roles = this.roles.findAll();
        final List<CapitalUser> allNonAdminUsers = Utilities.INSTANCE.getAllUsersExceptAdmin(this.users);
        CapitalUser selectedUser;

        if (operation == Operation.NEW)
        {
            selectedUser = new CapitalUser();
            selectedUser.username = "";
            selectedUser.email = "";
            selectedUser.fullName = "";
            HashSet<Role> defaultRoles = new HashSet<>();
            defaultRoles.add(roles.iterator().next());
            selectedUser.setRoles(defaultRoles);
            selectedUser.setActive(false);
        }
        else
        {
            selectedUser = this.users.findOne(username);
        }

        model.addAttribute("users", allNonAdminUsers);
        model.addAttribute("mode", this.getMode(operation));
        model.addAttribute("roles", roles);
        model.addAttribute("user", this.getCurrentUser(session));
        model.addAttribute("selecteduser", selectedUser);
        model.addAttribute("title", CapitalReportingApplication.APP_TITLE);
        model.addAttribute("version", CapitalReportingApplication.getVersion());

        return "configure";

    }

    private Operation getOperation(String mode)
    {
        Operation operation = Operation.NONE;
        switch (mode)
        {
            case "new":
                operation = Operation.NEW;
                break;
            case "ok_deleted":
                operation = Operation.DELETED;
                break;
            case "ok_modified":
                operation = Operation.MODIFIED;
                break;
            case "ok_added":
                operation = Operation.CREATED;
                break;
        }
        return operation;
    }

    private String getMode(Operation operation)
    {
        String mode = "";
        switch (operation)
        {
            case NEW:
                mode = "new";
                break;
            case DELETED:
                mode = "ok_deleted";
                break;
            case MODIFIED:
                mode = "ok_modified";
                break;
            case CREATED:
                mode = "ok_added";
                break;
        }
        return mode;
    }


}
