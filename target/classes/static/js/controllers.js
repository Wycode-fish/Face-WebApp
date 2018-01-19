var appControllers = angular
    .module('appControllers', [])
    .constant('emptyChart', {
        options: {credits: {enabled: false}},
        title: {text: ''},
        loading: true
    });

appControllers.controller("loginController", function ($scope, $http) {
    var historyMap = new Object();
    //初始化
    var pointType = "83p";
    var srcType = "imageClick";
    if ($scope.imageSrc == undefined) {
        $scope.imageSrc = "http://" + location.host + "/imgs/4.jpg";
        getLandmark(srcType, $scope.imageSrc);
    }

    //设置点数类型
    $scope.setPointType = function (ptStr) {
        pointType = ptStr;
        setPointTypeImgStyle(pointType);
        if ($scope.imageSrc != undefined){
            var temp = $scope.imageSrc.substr(0, 5);
            if (temp == "Face_"){
                srcType = "historyFileSelect";
                getHistoryImageLandmark($scope.imageSrc);
            }else {
                srcType = "imageClick";
                getLandmark(srcType, $scope.imageSrc);
            }
        }

    };

    $scope.uploadFile = function () {
        srcType = "fileSelect";
        var srcFileName = $scope.myFile.name;
        getLandmark(srcType, srcFileName);
    };

    $scope.imgClick = function () {
        srcType = "imageClick";
        var imgFile = event.srcElement;
        $scope.imageSrc = imgFile.src;
        getLandmark(srcType, $scope.imageSrc);
    };

    function removePoint() {
        var drawContain = document.getElementById("drawContain");
        drawContain.innerHTML = "";
    };

    //发送请求获取人脸关键点
    function getLandmark(srcType, srcFileName) {
        if (srcType == "imageClick") {
            $http({
                method: 'POST',
                url: '/getLandmarkPoints',
                params: {imgUrl: srcFileName, pointType: pointType}
            }).success(function (data) {
                drawPoints(data);
            });
        } else if (srcType == "fileSelect") {
            var slFileName = "SL_" + srcFileName;
            var faceFileName = "Face_" + srcFileName;
            var fd = new FormData();
            fd.append('file', $scope.myFile);
            fd.append("pointType", pointType);
            $http.post("/uploadFile", fd, {
                    transformRequest: angular.identity,
                    headers: {'Content-Type': undefined}
                })
                .success(function (data) {
                    drawPoints(data);
                    $scope.imageSrc = faceFileName;
                    //添加历史
                    var slImg = document.createElement("img");
                    slImg.style.width = 150 + 'px';
                    slImg.style.height = 150 + 'px';
                    slImg.style.marginRight = 6 + 'px';
                    slImg.src = slFileName;
                    slImg.onclick = function(){reGetLandmark(faceFileName)};
                    document.getElementById("imgDiv").appendChild(slImg);
                    historyMap[pointType + "+" + faceFileName] = data;
                })
                .error(function () {
                    alert("未能识别！");
                });
        } else if(srcType == "historyFileSelect"){
            $http({
                method: 'POST',
                url: '/getHistoryImageLandmarkPoints',
                params: {fileName: srcFileName, pointType: pointType}
            }).success(function (data) {
                if (data == undefined)
                    alert("未能识别！");
                else{
                    drawPoints(data);
                    historyMap[pointType + "+" + srcFileName] = data;
                }

            });
        }

    }

    //画点
    function drawPoints(data){
        removePoint();
        var faces = data["faces"];
        for (var i = 0; i < faces.length; i++) {
            var facePoints = faces[i]["facePoints"];
            for (var j = 0; j < facePoints.length; j++) {
                var point = facePoints[j];
                var pDiv = document.createElement("div");
                pDiv.setAttribute("class", "point");
                pDiv.style.marginLeft = point["x"] * 410 / 100 + "px";
                pDiv.style.marginTop = point["y"] * 410 / 100 + "px";
                document.getElementById("drawContain").appendChild(pDiv);
            }
        }
    }

    //重新获取
    function reGetLandmark(faceFileName){
        $scope.imageSrc = faceFileName;
        $scope.$apply();
        getHistoryImageLandmark(faceFileName);
    }

    function getHistoryImageLandmark(faceFileName) {
        var data = historyMap[pointType + "+" + faceFileName];
        if (data != undefined){
            drawPoints(data);
        }else{
            srcType = "historyFileSelect";
            getLandmark(srcType, faceFileName);
        }
    }

    function setPointTypeImgStyle(pointType) {
        switch (pointType) {
            case "5p" :
                document.getElementById("5p").src = "asster/5_click.png";
                document.getElementById("25p").src = "asster/25.png";
                document.getElementById("83p").src = "asster/83.png";
                break;
            case "25p" :
                document.getElementById("5p").src = "asster/5.png";
                document.getElementById("25p").src = "asster/25_click.png";
                document.getElementById("83p").src = "asster/83.png";
                break;
            case "83p" :
                document.getElementById("5p").src = "asster/5.png";
                document.getElementById("25p").src = "asster/25.png";
                document.getElementById("83p").src = "asster/83_click.png";
                break;
        }
    }

});

