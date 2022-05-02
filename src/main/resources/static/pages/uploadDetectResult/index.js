var uploadResultVue = new Vue({
	el: '#app',
	data() {
		return {
			form: {
				reportId: sessionStorage.getItem('reportId'),
				detectHospital: '',
				detectPerson: {
					name: '',
					idNumber:''
				},
				detectResult: ''
			},
			rules: {
				reportId: {
					required: true,
					message: '请输入报告单号',
					trigger: 'blur'
				},
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
					},
						{
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
		}
	},
	methods: {
		onSubmit(formName) {
			this.$refs[formName].validate((valid) => {
				if (valid) {
					axios.post("/upload/uploadDetectNATInfo", {natInfos: [this.form]}).then(res => {
						if (res.status === 200) {
							alert("提交成功！");
							sessionStorage.removeItem('reportId');
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

