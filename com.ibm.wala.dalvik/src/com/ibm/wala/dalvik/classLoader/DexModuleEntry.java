/*
 *
 * Copyright (c) 2009-2012,
 *
 *  Adam Fuchs          <afuchs@cs.umd.edu>
 *  Avik Chaudhuri      <avik@cs.umd.edu>
 *  Steve Suh           <suhsteve@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */

package com.ibm.wala.dalvik.classLoader;

import java.io.InputStream;

import org.jf.dexlib.ClassDefItem;

import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.ModuleEntry;

public class DexModuleEntry implements ModuleEntry {

    private final ClassDefItem classDefItem;
    private final String className;

    public DexModuleEntry(ClassDefItem cdefitems) {
        classDefItem = cdefitems;
        String temp =cdefitems.getClassType().getTypeDescriptor();
//      className = temp;
        if (temp.endsWith(";"))
            className = temp.substring(0,temp.length()-1); //remove last ';'
        else
            className = temp;
//      System.out.println(className);
    }

    public ClassDefItem getClassDefItem(){
        return classDefItem;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#asModule()
     */
    public Module asModule() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#getClassName()
     */
    public String getClassName() {
        return className;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#getInputStream()
     */
    public InputStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#getName()
     */
    public String getName() {
        return className;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#isClassFile()
     */
    public boolean isClassFile() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#isModuleFile()
     */
    public boolean isModuleFile() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.wala.classLoader.ModuleEntry#isSourceFile()
     */
    public boolean isSourceFile() {
        return false;
    }

	@Override
	public Module getContainer() {
		return asModule();
	}

}
