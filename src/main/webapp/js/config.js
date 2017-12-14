/**
 * Application Config
 */

var Config = {
    debug: true,
    devtools: true,
    silent: true,

    apiPath: '.',

    // DataTable默认配置
    dataTable_defaultOptions: {
        autoWidth: false,
        select: {style: 'multi'},
        pagingType: 'full_numbers',
        processing: true,
        language: {
            "sProcessing": '<i class="ace-icon fa fa-spinner fa-spin orange bigger-125"></i>&nbsp;&nbsp;处理中...',
            "sLengthMenu": "显示 _MENU_ 项结果",
            "sZeroRecords": "没有匹配结果",
            "sInfo": "显示第 _START_ 至 _END_ 项结果，共 _TOTAL_ 项",
            "sInfoEmpty": "显示第 0 至 0 项结果，共 0 项",
            "sInfoFiltered": "(由 _MAX_ 项结果过滤)",
            "sInfoPostFix": "",
            "sSearch": "搜索：",
            "sUrl": "",
            "sEmptyTable": "表中数据为空",
            "sLoadingRecords": "载入中...",
            "sInfoThousands": ",",
            "oPaginate": {
                "sFirst": "首页",
                "sPrevious": "上一页",
                "sNext": "下一页",
                "sLast": "末页"
            },
            "oAria": {
                "sSortAscending": ": 以升序排列此列",
                "sSortDescending": ": 以降序排列此列"
            }
        }
    },

    /**
     * daterangepicker locale
     */
    daterangepicker_locale: {
        zh_CN: {
            format: 'YYYY/MM/DD',
            applyLabel: '确定',
            cancelLabel: '取消',
            daysOfWeek: ["日","一","二","三","四","五","六"],
            monthNames: ["一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"]
        }
    }
};
