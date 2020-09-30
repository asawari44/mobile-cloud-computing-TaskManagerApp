const functions = require("firebase-functions");
const admin = require('firebase-admin');
const {tmpdir} = require("os");
const {Storage} = require("@google-cloud/storage");
const {dirname, join} = require("path");
const fs = require("fs-extra");
admin.initializeApp(functions.config().firebase);
const Jimp = require('jimp');

exports.resizeImg = functions
    .runWith({memory: "2GB", timeoutSeconds: 120})
    .storage
    .object()
    .onFinalize(async (object) => {
        console.log("initialized admin")
        const storage = admin.storage();
        storage.bucket();
        const bucket = storage.bucket();
        console.log(bucket);
        const filePath = object.name;
        console.log(filePath)
        const fileName = filePath.split("/").pop();
        console.log(fileName)
        const bucketDir = dirname(filePath);
        console.log(bucketDir);

        if (bucketDir.includes('project_media')) {
            if (fileName.includes('png') || fileName.includes('jpeg') || fileName.includes('jpg')) {
                const workingDir = join(tmpdir(), "resize");
                const tmpFilePath = join(workingDir, "source.png");

                console.log(`Got ${fileName} file`);

                if (fileName.includes("@") || !object.contentType.includes("image")) {
                    console.log(`Already resized. Exiting function`);
                    return false;
                }

                await fs.ensureDir(workingDir);
                await bucket.file(filePath).download({destination: tmpFilePath});

                const sizes = [100, 720, 400];

                const uploadPromises = sizes.map(async (size) => {

                    console.log(`Resizing ${fileName} at size ${size}`);
                    let ext = 'png';
                    if (fileName.split('.').pop()) {
                        ext = fileName.split('.').pop()
                    }

                    const imgName = fileName.replace(`.${ext}`, "");
                    const newImgName = `${imgName}@${size}.${ext}`;
                    const imgPath = join(workingDir, newImgName);


                    const image = await Jimp.read(tmpFilePath);
                    // Resize the image to width 150 and auto height.
                    await image.resize(size, Jimp.AUTO);
                    // Save and overwrite the image
                    await image.writeAsync(imgPath);
                    console.log(`Just resized ${newImgName} at size ${size}`);
                    return bucket.upload(imgPath, {
                        destination: join(bucketDir, newImgName)
                    });

                });

                await Promise.all(uploadPromises);

                return fs.remove(workingDir);
            } else {
                console.log("Content is not an image");
            }

        } else {
            console.log("returning not a working directory.")
            return
        }
    });


exports.sendAdminNotification = functions.database.ref('/notifications/').onWrite(event => {
    console.log("Executing new notification function.")
    const data = JSON.parse(JSON.stringify(event));
    const actualData = data.after;
    const ids = Object.keys(data.after);
    console.log(ids);

    ids.forEach((el) => {
        if (actualData[el] === 'Project') {
            const payload = {
                notification: {
                    title: 'New Project',
                    body: `You have been added in a new project.`
                }
            };
            admin.messaging().sendToTopic(el, payload)
                .then(function (response) {
                    console.log('Notification sent successfully:', response);
                    event.after.ref.parent.child('/notifications/' + el).set(
                        "Notification Sent"
                    );

                })
                .catch(function (error) {
                    console.log('Notification sent failed:', error);
                });
        } else if (actualData[el] === 'Task') {
            const payload = {
                notification: {
                    title: 'New Task',
                    body: `You have been assigned a new task.`
                }
            };
            admin.messaging().sendToTopic(el, payload)
                .then(function (response) {
                    console.log('Notification sent successfully:', response);
                    event.after.ref.parent.child('/notifications/' + el).set(
                        "Notification Sent"
                    );
                })
                .catch(function (error) {
                    console.log('Notification sent failed:', error);
                });
        }
    });

    return {
        message: "Success"
    }

});


function formatting(target) {
    return target < 10 ? '0' + target : target;
}

exports.scheduledFunctionPlainEnglish =
    functions.pubsub.schedule('every 10 minutes').onRun(async (context) => {
        admin.database().ref('/projects/').once('value', ((dataSnapshop) => {

            dataSnapshop.forEach((child) => {

                console.log(child.key, child.val());
                const project = child.val();

                if (project.deadline.split('-').length < 2) {
                    console.log("Date format is invalid")

                } else {
                    let day = parseInt(project.deadline.split('-')[0]);


                    const timeNow = new Date();


                    let currentDate = parseInt(formatting(timeNow.getDay()));
                    console.log('Current :', currentDate, 'Project deadline: ', day)

                    if (currentDate !== day && currentDate + 1 !== day) {
                        console.log("The project deadline isn't nearby");
                    } else {
                        const payload = {
                            notification: {
                                title: 'Project Deadline is near',
                                body: `You have less than 1 day to complete ${project.name}`
                            }

                        };
                        const members = project.members;
                        console.log(members);
                        members.forEach((el) => {
                            console.log(el);
                            admin.messaging().sendToTopic(el.userId, payload)
                                .then(function (response) {
                                    console.log('Notification sent successfully:', response);

                                })
                                .catch(function (error) {
                                    console.log('Notification sent failed:', error);
                                });
                        })
                    }
                }


            })
        }));
        return {
            "message": "success"
        }
    });