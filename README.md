# Android-RTEditor

[![Images](http://www.1gravity.com/messagecompose.png)]

The Android RTEditor is a rich text editor component for Android that can be used as a drop in for EditText.

Features
--------

The editor offers the following <b>character formatting</b> options:

<ul>
<li><b>Bold</b></li>
<li><i>Italic</i></li>
<li><u>Underline</u></li>
<li><strike>Strike through</strike></li>
<li><sup>Superscript</sup></li>
<li><sub>Subscript</sub></li>
<li>Text size</li>
<li>Text color</li>
<li>Background color</li>
</ul>

It also includes the following <b>paragraph formatting</b> options:

<ol><li>Numbered</li></ol>
<ul>
<li>Bullet points</li>
<ul style='list-style-type:none;'>Indentation</ul>
<div align="left">Left alignment</div>
<div align="center">Center alignment</div>
<div align="right">Right alignment</div>
</ul>

and [links](https://www.1gravity.com)<br>
and images: [![Images](http://www.1gravity.com/smiley.jpg)](https://www.1gravity.com)


The 3 main RTEditor components
------------------------------
1. <b>RTEditText</b>: is the EditText drop in component. Add it to your layout like you would EditText:
```xml
  <com.onegravity.rteditor.RTEditText
    android:id="@+id/rtEditText_1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:imeOptions="actionDone|flagNoEnterAction"
    android:inputType="textMultiLine|textAutoCorrect|textCapSentences" />
```
In code you would typically use methods to set and get the text content:
- set text: <code>RTEditText.setRichTextEditing(true, "My content");</code>
- get text: <code>RTEditText.getText(RTFormat.HTML)</code>

2. <b>RTToolbar</b>: is an interface for the toolbar used to apply text and paragraph formatting and other features listed above. The actual RTToolbar implementation is in a separate module and is a scrollable ribbon but alternative implementations aren't too hard to realize (popup, action buttons, floating buttons...). The toolbar implementation is easy to integrate into your layout:
```xml
  <include layout="@layout/rte_toolbar" />
```

 or if you want to have two ribbons for character and paragraph formatting:
```xml
  <include layout="@layout/rte_toolbar_character" />
  <include layout="@layout/rte_toolbar_paragraph" />
```

In code you'd typically not interact with the toolbar (see RTManager below for the one exception).

3. RTManager: is the glue that holds the rich text editor components (RTEditText), the toolbar and your app together. Each rich text editor component and each toolbar needs to be registered with the RTManager before they are functional. Multiple editors and multiple toolbars can be registered. The RTManager is instantiated by your app in code usually in the onCreate passing in an RTApi object that connects your and the rich text editor.
A typical initialization process looks like this:

<code>
// create RTManager
RTApi rtApi = new RTApi(this, new RTProxyImpl(this), new RTMediaFactoryImpl(this, true));
RTManager rtManager = new RTManager(rtApi, savedInstanceState);

// register toolbar
HorizontalRTToolbar rtToolbar = (HorizontalRTToolbar) findViewById(R.id.rte_toolbar);
if (rtToolbar != null) {
  rtManager.registerToolbar(toolbarContainer, rtToolbar);
}

// register editor & set text
RTEditText rtEditText = (RTEditText) findViewById(R.id.rtEditText);
rtManager.registerEditor(rtEditText, true);
rtEditText.setRichTextEditing(true, message);

</code>

Demo project
------------

The project consists of 4 different modules:
- ColorPicker: a color picker based on this: https://github.com/LarsWerkman/HoloColorPicker. The RTEditor uses an enhanced version that allows to enter ARGB values, includes a ColorPickerPreference that can be used in preference screens and shows a white and gray chessboard pattern behind the color visible when the the alpha channel is changed and the color becomes (partly) transparent.
- MaterialDialog: a library based on this: https://github.com/afollestad/material-dialogs. The RTEditor uses a trimmed-down version because it only uses a subset of its functionality for the link dialog.
- RTEditor: the actual rich text editor (excluding the toolbar implementation)
- RTEditor-Toolbar: the toolbar implementation

The app module itself contains a sample app that shows how to use the rich text editor component.
