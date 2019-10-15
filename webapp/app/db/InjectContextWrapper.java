/* InjectContextWrapper.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2015 Universiteit Gent
 * 
 * This file is part of the Rasbeb project, an interactive web
 * application for Bebras competitions.
 * 
 * Corresponding author:
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Rasbeb Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Rasbeb Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Rasbeb Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package db;

import be.bebras.rasbeb.db.DataAccessContext;
import be.bebras.rasbeb.db.data.Role;
import play.Play;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Implements the {@link InjectContext} annotation.
 */
public class InjectContextWrapper extends Action<InjectContext> {

    @Override
    public F.Promise<Result> call(Http.Context httpContext) throws Throwable {

        // obtain id, role and language for the data access context
        int id = 0;
        Role role = Role.ANONYMOUS;

        Http.Session session = httpContext.session();
        String idString = session.get("id");
        if (idString != null) {
            id = Integer.parseInt(idString);
            role = Role.valueOf(session.get("role"));
        }

        Http.Cookie cookie = httpContext.request().cookie(Play.langCookieName());
        String lang = cookie == null ? "en" : cookie.value();

        // create a context, and run the action delegate in that transaction
        try (DataAccessContext context = DataAccess.getContext(id, lang, role)) {
            httpContext.args.put("data-access-context", context);

            if (configuration.inTransaction()) {
                try {
                    context.begin();
                    F.Promise<Result> result = delegate.call(httpContext);
                    context.commit();
                    return result;
//                } finally {
//                    context.rollback();
//                }
                } catch (Exception ex) {
                    context.rollback();
                    throw ex;
                }
            } else {
                return delegate.call(httpContext);
            }
        }
    }
}
