var vm = new Vue({
	el: '#app',
	data() {
		return {
			form: {
				isSamplingPerson: {
					name: '',
					idNumber: ''
				},
				samplingPerson: {
					name: '',
					idNumber: ''
				},
				samplingPlace: ''
			},
			rules: {
				isSamplingPerson: {
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
				samplingPerson: {
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
				samplingPlace: {
					required: true,
					message: '请输入采样地址',
					trigger: 'blur'
				}
			}
		}
	},
	methods: {
		onSubmit() {
			this.$refs['form'].validate((valid) => {
				if (valid) {
					axios.post("/upload/uploadSamplingNATInfo", {natInfos: [this.form]}).then(res => {
						if (res.status === 200) {
							alert("提交成功！")
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

