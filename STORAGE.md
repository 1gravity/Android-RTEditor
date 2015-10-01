## Custom Storage Scenarios

The default implementation RTMediaFactoryImpl merely stores embedded images in the primary external filesystem or the internal filesystem.
By implementing a custom RTMediaFactory you can store media files in an SQLite database, in cloud storage or any other place you could think of.
When loading the stored rich text, Media files can be retrieved by any means thinkable (from the database, through a ContentProvider, over the network...).

This can be achieved by using a custom RTMediaFactory and custom RTMedia implementations (and potentially custom RTFormats).
The basic concept for a custom storage implementation is to have a RTMediaFactory that creates RTMedia objects containing the information to store/load the media files.
This information can depend on what step of the editing process we're currently in:

1. Insert media objects into the rich text: the media factory would create RTMedia objects pointing to the actual media file as created by the rich text editor.
Theoretically you could already process the file and put them e.g. into a database but since we don't know if the media object remains in the rich text (the user could delete it) this would be premature.
Media objects are created by calling the createXYZ(RTMediaSource) method of the RTMediaFactory with RTMediaSource giving access to the original media file.

2. Saving media objects along with the rich text into some permanent storage: now's the time to implement the actual storage scenario.
On one side you'd need to retrieve all media objects from the editor: RTEditText.getRichText(RTFormat.Html) would return a RTHtml object which has a getImages() method to get all embedded images.
These images would need to be stored according to your scenario.

The RTMedia object can return an arbitrary "file path" in getFilePath depending on the RTFormat parameter.
This could be a simple file path ("file://:), a ContentProvider uri ("content://), or a cid: reference for html links.

3. Load the media objects as part of the rich text into the editor

The RTFormat used to retrieve the file path for a media object helps to identify what part of the editing process is happening at the moment:
* text editor.
That information can differ depending on 

Depending on what format is needed (defined by RTFormat) the RTMedia would  

Please read the javadoc for RTMediaFactory explains the process media files go through so please.   

