package org.geopublishing.geopublisher.swing;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.JComponent;

/**
 * Interface for all methods needed for a html editor.
 * This interface helps to switch between several
 * implementations of an html viewer/editor. 
 */
public interface HTMLEditPaneInterface /*extends HTMLInfoPaneInterface*/ {

  /**
   * GUI component of the html view. This method usually should
   * return {@code 'this'}.
   * If the implementation does not extends a swing component
   * this method has to perform a warp!
   */
  public JComponent getComponent();
  
  /**
   * Indicates whether the html view already has its own scroll
   * pane. This helps the application to decide whether or not it is
   * necessary to create one.
   */
  public boolean hasScrollPane();

  
  /**
   * Returns the preferred dialog size for the editor. Because of the
   * menu structure some editors might need a bigger windows than others. 
   * If this method returns <code>null</code> a default size is used. 
   */
  public Dimension getPreferredSize();
  
  /**
   * Adds a tab pane to edit a HTML document.
   * @param title  tab title
   * @param url    URL of the document to be edit
   * @param idx    index number for the document title if a new
   *               file is created
   */
  public void addEditorTab(String title, URL url, int idx);
  
  /**
   * Removes all tabs.
   */
  public void removeAllTabs();


  /**
   * Called when surrounded window/dialog/application is closed.
   * Should perform editor specific actions (e.g. save operation).
   * @param source object which initiates the closing
   * @return <code>true</code> if the dialog should be closed by
   *         the surrounding application 
   */
  public boolean performClosing(Object source);

}