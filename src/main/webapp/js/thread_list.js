var thread_list = new Vue({
  el: '#thread_list',
  data: {
    servers: [
      { ipAndPort: '192.168.3.119:9095',weight:1 },
      { ipAndPort: '192.168.3.63:9095',weight:2 },
      { ipAndPort: '192.168.3.116:9095',weight:5 }
    ]
  }
})