// Copyright (C) 2001-2003 Jon A. Maxwell (JAM)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.sourceforge.jnlp;

import java.util.*;

/**
 * The application-desc element.
 *
 * @author <a href="mailto:jmaxwell@users.sourceforge.net">Jon A. Maxwell (JAM)</a> - initial author
 * @version $Revision: 1.7 $
 */
public class ApplicationDesc implements LaunchDesc {

    /** the main class name and package */
    private final String mainClass;

    /** the arguments */
    private  String arguments[];
    private final boolean fx;

    /**
     * Create an Application descriptor.
     *
     * @param mainClass the main class name and package
     * @param arguments the arguments
     */
    public ApplicationDesc(String mainClass, String[] arguments, boolean isFX) {
        this.mainClass = mainClass;
        this.arguments = arguments;
        this.fx = isFX;
    }

    /**
     * @return the main class name
     */
    @Override
    public String getMainClass() {
        return mainClass;
    }

    /**
     * @return the arguments
     */
    public String[] getArguments() {
        return arguments.clone();
    }

    /**
     * Add an argument to the end of the arguments.
     * @param arg argument of command
     */
    public void addArgument(String arg) {
        List<String> l = new ArrayList<>(Arrays.asList(arguments));
        l.add(arg);

        arguments = l.toArray(arguments);
    }

}
