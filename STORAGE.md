## Custom Storage Scenarios

The default implementation for RTMediaFactory is RTMediaFactoryImpl, which stores embedded images in the primary external filesystem or the internal filesystem.
By implementing a custom RTMediaFactory you can store media files in an SQLite database, in cloud storage or any other place you could think of.
When loading the stored rich text, media files can be retrieved by any means thinkable (directly from the database, through a ContentProvider, over the network...).

This can be achieved by using a custom RTMediaFactory and custom RTMedia implementations (and potentially custom RTFormats).
The basic concept for a custom storage implementation is to have an RTMediaFactory that creates RTMedia objects containing the information to store/load the media files.
This information can depend on what step of the editing process we're currently in:

1. **Insert media objects** into the rich text: the media factory would create RTMedia objects pointing to the actual media file as created by the rich text editor.
Theoretically you could already process the file and put them e.g. into a database but since we don't know if the media object remains in the rich text (the user could delete it) this would be premature.
Media objects are created by calling the RTMediaFactory.createXYZ(RTMediaSource) method with RTMediaSource giving access to the original media file.

2. **Saving media objects** along with the rich text into some permanent storage.
On one side you'd need to retrieve all media objects from the editor: RTEditText.getRichText(RTFormat.Html) returns a RTHtml object with a getImages() method to get all embedded images.
These images would need to be stored according to your storage scenario.
On the other side the RTMedia objects created when calling the RTEditText.getRichText would have to return the path that allows to find the stored images when the text is loaded later on.
Let's say the images are stored in an SQLite database and the rich text itself as HTML text.
The call to RTEditText.getRichText(RTFormat.Html) would convert the rich text from its Spanned format to HTML. The RTImage objects created during that process could return a file path like this:
cid:&lt;id&gt with id being some id to the database record for that image. The cid:&lt;id&gt would be put into the src attribute for that image: &lt;img src="cid:&lt;id&gt;"&gt;.
When the text is loaded later on, the RTMediaFactory could retrieve the id and through that the image from the database.

3. **Load media objects** as part of the rich text into the editor.
The RTMediaFactory would create an RTMedia object from the path passed into createImage(String).
Knowing what format the persistent file path has, it can distinguish between the different use cases.
E.g. if it encounters a cid:&lt;id&gt path, it would know to load the file from the permanent storage.
Important: the image needs to have an absolute file path because ImageSpan can handle only that. That means an image stored in a database or any other non-filesystem based storage needs to be extracted/copied to the file system.
If the file path doesn't start with cid: it would assume the media file is stored in the file system and uses the path as an absolute path but all kinds of scenarios are thinkable here: content://, https:// or any custom scheme could be used to access media files in different ways.


The rich text editor is used in our email client. Embedded media files are stored in different places depending on the use case. Images in signatures are stored in the file system, while images when composing emails are initially stored in the file system and then stored in a database once the mail is saved.
When writing emails with a signature that contains an image, that image would initially be retrieved from the file system (since it's part of the original signature) but once the mail is saved the whole email including the embedded image goes to the database.
When sending an email that same email needs to retrieve the message text with links to the embedded images in an RFC 2392 compliant format. All together we use five different formats for the file path.

While the storage API seems to be complex, it's also powerful enough to support any storage scenario imaginable.