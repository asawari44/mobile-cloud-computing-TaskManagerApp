Let's code in this branch.


Uploading a generic User logic activity based project, you may remove extra activities if they are not needed.


Steps to link android studio to gitlab:

prerequisites:

install git bash and add your own ssh key to gitlab repository.
(You can take help of web software development assignment to do so. First 3 exercises are for the same purpose)

Steps:
1. Go to android studio , select VCS -> checkout from version control -> git
2. enter this url in URL tab :
   git@version.aalto.fi:CS-E4100/mcc-fall-2019-g03.git
3. Test the connection, if the public key part mentioned in prerequesites is done, this step should return success.
4. Clone the repository.


How to commit :
1. Check you are on the dev branch (VCS -> git -> branches) will show you local branch you are using.
2. Before committing any changes, pull the latest code always.
   VCS -> git -> pull
3. Once the changes are pulled, You have to add the files in which you made changes.
   VCS -> git -> commit files..
   select files which you need to push and enter appropriate msg in the commit box
4. After commit, you need to push the changes to remote dev branch
   VCS-> git -> push 
        