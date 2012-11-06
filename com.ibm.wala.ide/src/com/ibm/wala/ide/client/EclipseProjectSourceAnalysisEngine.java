/******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.ide.client;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ide.plugin.CorePlugin;
import com.ibm.wala.ide.util.EclipseFileProvider;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ide.util.EclipseProjectPath.Loader;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;

/**
 * An {@link EclipseProjectAnalysisEngine} specialized for source code analysis with CAst
 */
abstract public class EclipseProjectSourceAnalysisEngine<P> extends EclipseProjectAnalysisEngine<P> {

  public static final String defaultFileExt = "java";

  /**
   * file extension for source files in this Eclipse project
   */
  final String fileExt;

  public EclipseProjectSourceAnalysisEngine(P project) throws IOException, CoreException {
    this(project, defaultFileExt);
  }

  public EclipseProjectSourceAnalysisEngine(P project, String fileExt) throws IOException, CoreException {
    super(project);
    this.fileExt = fileExt;
    try {
      setExclusionsFile((new EclipseFileProvider()).getFileFromPlugin(CorePlugin.getDefault(), "J2SEClassHierarchyExclusions.txt")
          .getAbsolutePath());
    } catch (IOException e) {
      Assertions.UNREACHABLE("Cannot find exclusions file");
    }
  }

  @Override
  public AnalysisCache makeDefaultCache() {
    return new AnalysisCache(AstIRFactory.makeDefaultFactory());
  }

  protected abstract AnalysisScope makeSourceAnalysisScope();

  @Override
  public void buildAnalysisScope() {
    try {
      scope = makeSourceAnalysisScope();
      if (getExclusionsFile() != null) {
        scope.setExclusions(FileOfClasses.createFileOfClasses(new File(getExclusionsFile())));
      }
      EclipseProjectPath<?,?> epath = getEclipseProjectPath();

      for (Module m : epath.getModules(Loader.PRIMORDIAL, true)) {
        scope.addToScope(scope.getPrimordialLoader(), m);
      }
      ClassLoaderReference app = scope.getApplicationLoader();
      ClassLoaderReference src = getSourceLoader();
      for (Module m : epath.getModules(Loader.APPLICATION, true)) {
        if (m instanceof SourceDirectoryTreeModule) {
          scope.addToScope(src, m);
        } else {
          scope.addToScope(app, m);
        }
      }
      for (Module m : epath.getModules(Loader.EXTENSION, true)) {
        if (!(m instanceof BinaryDirectoryTreeModule))
          scope.addToScope(app, m);
      }
      /*
       * ClassLoaderReference src = ((JavaSourceAnalysisScope)scope).getSourceLoader(); for (Module m :
       * epath.getModules(Loader.APPLICATION, false)) { scope.addToScope(src, m); }
       */

    } catch (IOException e) {
      Assertions.UNREACHABLE(e.toString());
    }
  }

  protected abstract ClassLoaderReference getSourceLoader();

  @Override
  public AnalysisOptions getDefaultOptions(Iterable<Entrypoint> entrypoints) {
    AnalysisOptions options = new AnalysisOptions(getScope(), entrypoints);

    SSAOptions ssaOptions = new SSAOptions();
    ssaOptions.setDefaultValues(new SSAOptions.DefaultValues() {
      public int getDefaultValue(SymbolTable symtab, int valueNumber) {
        return symtab.getDefaultValue(valueNumber);
      }
    });

    options.setSSAOptions(ssaOptions);

    return options;
  }
}
