package net.topikachu.kieserver;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import java.security.Principal;
import java.security.acl.Group;
import java.util.*;

@Component
public class JaccSpringAdapter implements PolicyContextHandler {
    @PostConstruct
    public void register() throws PolicyContextException {
        PolicyContext.registerHandler("javax.security.auth.Subject.container", this, false);

    }

    public boolean supports(String key) throws PolicyContextException {
        if ("javax.security.auth.Subject.container".equals(key)) {
            return true;
        }

        return false;
    }

    public String[] getKeys() throws PolicyContextException {
        return new String[]{"javax.security.auth.Subject.container"};
    }

    public Object getContext(String key, Object data)
            throws PolicyContextException {


        Set<Principal> principals = new HashSet<Principal>();
        principals.add(() -> "admin");

        principals.add(getGroup());

        final Subject s = new Subject(false, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);
        return s;
    }

    protected Group getGroup() {
        Group group = new Group() {

            private List<Principal> members = new ArrayList<Principal>();

            public String getName() {
                return "Roles";
            }

            public boolean removeMember(Principal user) {
                return members.remove(user);
            }

            public Enumeration<? extends Principal> members() {

                return Collections.enumeration(members);
            }

            public boolean isMember(Principal member) {
                return members.contains(member);
            }

            public boolean addMember(Principal user) {

                return members.add(user);
            }
        };


        Arrays.asList("admin", "kieserver").forEach(
                roleName -> {
                    group.addMember(new Principal() {

                        public String getName() {
                            return roleName;
                        }
                    });
                }
        );


        return group;
    }
}
