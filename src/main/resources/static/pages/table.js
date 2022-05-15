var vm = new Vue({
    el: "#table",
    data: {
        inputForReportId: '',
        inputForIdNumber: '',
        options: [{
            value: '阴性',
            label: '阴性'
        }, {
            value: '阳性',
            label: '阳性'
        }],
        selectValue: '',
        samplingTime: [],
        pageTable: {
            pageNum: 1,
            pageSize: 10,
            size: 1,
            total: 0,
            pages: 1,
            tableData: []
        },
        dialogFormVisible: false,
        uploadResultForm: {
            reportId: '',
            detectHospital: '',
            detectPerson: {
                name: '',
                idNumber:''
            },
            detectResult: ''
        },
        rules: {
            detectHospital: {
                required: true,
                message: '请输入检测机构',
                trigger: 'blur'
            },
            detectPerson: {
                name: {
                    required: true,
                    message: '请输入姓名',
                    trigger: 'blur'
                },
                idNumber: [{
                    required: true,
                    message: '请输入正确的身份证号',
                    trigger: 'blur'
                }, {
                    min: 18,
                    message: '请输入18位身份证号',
                    trigger: 'blur'
                }],
            },
            detectResult: {
                required: true,
                message: '请选择检测结果',
                trigger: 'change'
            }
        },
    },
    created: function () {
        axios.post("/query/queryPageNATInfos", {"pageNum": this.pageTable.pageNum,"pageSize": this.pageTable.pageSize}).then(res => {
            this.pageTable.tableData = res.data.list;
            this.pageTable.size = res.data.size;
            this.pageTable.total = res.data.total;
            this.pageTable.pages = res.data.pages;
        })
    },
    methods: {
        indexMethod(index) {
            return index + (this.pageTable.pageNum - 1) * this.pageTable.pageSize + 1;
        },
        handleSizeChange(val) {
            axios.post("/query/queryPageNATInfos", {"pageNum": this.pageTable.pageNum,"pageSize": val}).then(res => {
                this.pageTable.tableData = res.data.list;
                this.pageTable.pageSize = res.data.pageSize;
                this.pageTable.size = res.data.size;
                this.pageTable.total = res.data.total;
                this.pageTable.pages = res.data.pages;
            })
        },
        handleCurrentChange(val) {
            axios.post("/query/queryPageNATInfos", {"pageNum": val,"pageSize": this.pageTable.pageSize}).then(res => {
                this.pageTable.tableData = res.data.list;
                this.pageTable.pageNum = res.data.pageNum;
                this.pageTable.size = res.data.size;
                this.pageTable.total = res.data.total;
                this.pageTable.pages = res.data.pages;
            })
        },
        queryByInput(){
            var params = {
                "pageNum": this.pageTable.pageNum,
                "pageSize": this.pageTable.pageSize,
                "reportId": this.inputForReportId,
                "idNumber": this.inputForIdNumber,
                "detectResult": this.selectValue,
                "samplingTimeBegin": this.samplingTime[0],
                "samplingTimeEnd": this.samplingTime[1]
            };
            axios.post("/query/queryPageNATInfos", params).then(res => {
                this.pageTable.tableData = res.data.list;
                this.pageTable.pageNum = res.data.pageNum;
                this.pageTable.pageSize = res.data.pageSize;
                this.pageTable.size = res.data.size;
                this.pageTable.total = res.data.total;
                this.pageTable.pages = res.data.pages;
            });
        },
        clearInput(){
            this.inputForReportId = '';
            this.inputForIdNumber = '';
            this.selectValue = '';
            this.samplingTime = [];
            this.queryByInput();
        },
        uploadResult(prop) {
            this.uploadResultForm.reportId = prop.reportId;
            this.dialogFormVisible = true;
        },
        onSubmit(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    axios.post("/upload/uploadDetectNATInfo", {natInfos: [this.uploadResultForm]}).then(res => {
                        if (res.status === 200) {
                            alert("提交成功！");
                            this.dialogFormVisible = false;
                        }
                    }).catch(function (error) {
                        alert("提交失败！")
                    })
                } else {
                    console.log('error submit!!');
                    return false;
                }
            });
        }
    }
})