function deleteAllNotes(params, context,done) {
    var token = context.getAccessToken();
    if (!token) {
        //Not login
        done({result:-1, msg:"Must login first"});
    }
    KiiUser.authenticateWithToken(token, {
            success: function(theUser) {
                console.log("User authenticated!");
                deleteAllUserNotes(theUser, done);
        },
        failure: function(theUser, errorString) {
            console.log("Error authenticating: " + errorString);
            done({result:-2, msg:errorString});
        }
    });

}

function deleteAllUserNotes(user, done) {
    var bucket = user.bucketWithName("notes");
    bucket['delete']({
      success: function(deletedBucket) {
          done({result:0});
      },
      
      failure: function(bucketToDelete, anErrorString) {
          done({result:-3, msg:anErrorString});
      }
    });    
}
